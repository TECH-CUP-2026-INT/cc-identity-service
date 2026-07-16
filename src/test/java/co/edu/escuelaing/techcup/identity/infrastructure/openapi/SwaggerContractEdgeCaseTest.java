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

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerContractEdgeCaseTest {

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
    void openApiDocumentsRequestSchemasForEveryPublicCommandEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/v1/auth/login']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/LoginRequest")))
                .andExpect(jsonPath("$['paths']['/api/v1/auth/login/google']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/GoogleLoginRequest")))
                .andExpect(jsonPath("$['paths']['/api/v1/otp/validate']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/OtpValidationRequest")))
                .andExpect(jsonPath("$['paths']['/api/v1/otp/resend']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/OtpResendRequest")))
                .andExpect(jsonPath("$['paths']['/api/v1/password/recovery']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/PasswordRecoveryRequest")))
                .andExpect(jsonPath("$['paths']['/api/v1/password/reset']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/PasswordResetRequest")))
                .andExpect(jsonPath("$['paths']['/api/v1/internal/credentials']['post']['requestBody']['content']['application/json']['schema']['$ref']",
                        startsWith("#/components/schemas/CreateCredentialRequest")));
    }

    @Test
    void openApiDocumentsRequiredFieldsAndAuditQueryParameters() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['components']['schemas']['LoginRequest']['required']", hasItems("email", "password")))
                .andExpect(jsonPath("$['components']['schemas']['OtpValidationRequest']['required']", hasItems("userId", "otpCode")))
                .andExpect(jsonPath("$['components']['schemas']['PasswordResetRequest']['required']", hasItems("email", "recoveryCode", "newPassword")))
                .andExpect(jsonPath("$['components']['schemas']['CreateCredentialRequest']['required']", hasItems("userId", "email", "password", "fullName", "userType", "role")))
                .andExpect(jsonPath("$['paths']['/api/v1/audit']['get']['parameters'][*]['name']",
                        hasItems("startDate", "endDate", "actionType", "userId")));
    }

    @Test
    void openApiDocumentsBearerJwtSchemeAndGlobalSecurityRequirement() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['components']['securitySchemes']['Bearer Authentication']['type']").value("http"))
                .andExpect(jsonPath("$['components']['securitySchemes']['Bearer Authentication']['scheme']").value("bearer"))
                .andExpect(jsonPath("$['components']['securitySchemes']['Bearer Authentication']['bearerFormat']").value("JWT"))
                .andExpect(jsonPath("$['security'][0]['Bearer Authentication']").isArray());
    }
}
