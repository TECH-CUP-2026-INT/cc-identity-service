package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuditQueryUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.AuditEventMapper;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
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

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditQueryUseCase auditQueryUseCase;
    @MockBean
    private AuditEventMapper auditEventMapper;

    @Test
    void queryEventsReturnsMappedAuditEventsAndPassesFilters() throws Exception {
        LocalDateTime startDate = LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, Month.JANUARY, 31, 23, 59);
        var event = TestFixtures.auditEvent();
        var response = TestFixtures.auditEventResponse();
        when(auditQueryUseCase.queryEvents(startDate, endDate, AuditActionType.USER_LOGIN, TestFixtures.USER_ID))
                .thenReturn(List.of(event));
        when(auditEventMapper.toResponse(event)).thenReturn(response);

        mockMvc.perform(get("/api/v1/audit")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-01-31T23:59:00")
                        .param("actionType", "USER_LOGIN")
                        .param("userId", TestFixtures.USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("audit-1"))
                .andExpect(jsonPath("$[0].userId").value(TestFixtures.USER_ID.toString()))
                .andExpect(jsonPath("$[0].actionType").value("USER_LOGIN"));

        verify(auditQueryUseCase).queryEvents(startDate, endDate, AuditActionType.USER_LOGIN, TestFixtures.USER_ID);
    }

    @Test
    void queryEventsWorksWithoutOptionalFilters() throws Exception {
        when(auditQueryUseCase.queryEvents(null, null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(auditQueryUseCase).queryEvents(null, null, null, null);
    }
}
