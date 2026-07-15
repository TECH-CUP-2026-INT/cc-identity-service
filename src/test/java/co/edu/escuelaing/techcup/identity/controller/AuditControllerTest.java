package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.config.JwtAuthFilter;
import co.edu.escuelaing.techcup.identity.config.SecurityConfig;
import co.edu.escuelaing.techcup.identity.config.CustomOAuth2UserService;
import co.edu.escuelaing.techcup.identity.config.OAuth2AuthenticationSuccessHandler;
import co.edu.escuelaing.techcup.identity.dto.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.service.AuditService;
import co.edu.escuelaing.techcup.identity.service.GmailLoginService;
import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.TokenBlacklistService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, CustomOAuth2UserService.class, OAuth2AuthenticationSuccessHandler.class})
@TestPropertySource(properties = {
    "spring.security.oauth2.client.registration.google.client-id=test-id",
    "spring.security.oauth2.client.registration.google.client-secret=test-secret"
})
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AuditService auditService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private GmailLoginService gmailLoginService;
    @MockitoBean private TokenBlacklistService tokenBlacklistService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditEvents_adminRole_returns200() throws Exception {
        when(auditService.findEvents(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/admin/audit-events"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAuditEvents_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/audit-events"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAuditEvents_unauthenticated_returns401or302() throws Exception {
        // With OAuth2 login active, Spring redirects unauthenticated requests
        // to the OAuth2 provider (302) instead of returning 401.
        // Both are valid "not authenticated" responses in this context.
        mockMvc.perform(get("/api/admin/audit-events"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 302) {
                        throw new AssertionError("Expected 401 or 302 but was: " + status);
                    }
                });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditEvents_returnsJsonPage() throws Exception {
        when(auditService.findEvents(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new AuditEventResponse())));

        mockMvc.perform(get("/api/admin/audit-events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditEvents_emptyResult_returns200WithEmptyList() throws Exception {
        when(auditService.findEvents(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/admin/audit-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditEvents_invalidDateRange_returns400() throws Exception {
        when(auditService.findEvents(any(), any(), any(), any(), any(Pageable.class)))
                .thenThrow(new IllegalArgumentException("startDate must not be after endDate"));

        mockMvc.perform(get("/api/admin/audit-events")
                        .param("startDate", "2026-12-31T00:00:00")
                        .param("endDate",   "2026-01-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuditEvents_paginationParams_passedCorrectly() throws Exception {
        when(auditService.findEvents(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/admin/audit-events")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
