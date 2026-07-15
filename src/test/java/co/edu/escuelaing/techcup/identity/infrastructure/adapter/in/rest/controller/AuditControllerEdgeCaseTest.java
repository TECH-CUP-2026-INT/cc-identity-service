package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.port.in.AuditQueryUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.AuditEventMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuditController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuditControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditQueryUseCase auditQueryUseCase;
    @MockBean
    private AuditEventMapper auditEventMapper;

    @Test
    void queryEventsRejectsInvalidActionTypeQueryParam() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .param("actionType", "NOT_A_REAL_ACTION"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter: actionType"));

        verifyNoInteractions(auditQueryUseCase, auditEventMapper);
    }

    @Test
    void queryEventsRejectsInvalidStartDateFormat() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .param("startDate", "2026/01/01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter: startDate"));

        verifyNoInteractions(auditQueryUseCase, auditEventMapper);
    }

    @Test
    void queryEventsRejectsStartDateAfterEndDate() throws Exception {
        mockMvc.perform(get("/api/v1/audit")
                        .param("startDate", "2026-02-01T00:00:00")
                        .param("endDate", "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_DATE_RANGE"))
                .andExpect(jsonPath("$.message").value("startDate must be before or equal to endDate"));

        verifyNoInteractions(auditQueryUseCase, auditEventMapper);
    }
}
