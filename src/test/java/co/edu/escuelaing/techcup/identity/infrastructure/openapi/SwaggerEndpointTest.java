package co.edu.escuelaing.techcup.identity.infrastructure.openapi;

import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.GoogleOAuthPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RecoveryTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepositoryPort userRepositoryPort;
    @MockBean
    private OtpRepositoryPort otpRepositoryPort;
    @MockBean
    private AuditEventRepositoryPort auditEventRepositoryPort;
    @MockBean
    private EmailPort emailPort;
    @MockBean
    private GoogleOAuthPort googleOAuthPort;
    @MockBean
    private RecoveryTokenRepositoryPort recoveryTokenRepositoryPort;
    @MockBean
    private RevokedTokenRepositoryPort revokedTokenRepositoryPort;
    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void openApiDocsEndpointIsPublicAndDocumentsExpectedIdentityPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("TechCup Identity Service API"))
                .andExpect(jsonPath("$['components']['securitySchemes']['Bearer Authentication']['scheme']").value("bearer"))
                .andExpect(jsonPath("$['paths']['/api/v1/auth/login']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/auth/login/google']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/otp/validate']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/password/recovery']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/token/validate']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/auth/logout']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/register/admin-organizer']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/internal/credentials']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/v1/audit']['get']").exists());
    }

    @Test
    void swaggerUiEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
