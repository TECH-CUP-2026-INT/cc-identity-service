package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.GetUserEmailUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.RevokeUserSessionsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.UpdateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InternalCredentialController.class,
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
class InternalCredentialControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateCredentialsUseCase createCredentialsUseCase;
    @MockBean
    private GetUserEmailUseCase getUserEmailUseCase;
    @MockBean
    private RevokeUserSessionsUseCase revokeUserSessionsUseCase;
    @MockBean
    private UpdateCredentialsUseCase updateCredentialsUseCase;
    @MockBean
    private UserMapper userMapper;

    @Test
    void createCredentialsRejectsAllMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"))
                .andExpect(jsonPath("$.errors.fullName").value("Full name is required"))
                .andExpect(jsonPath("$.errors.userType").value("User type is required"))
                .andExpect(jsonPath("$.errors.role").value("Role is required"));

        verifyNoInteractions(createCredentialsUseCase, userMapper);
    }

    @Test
    void createCredentialsRejectsInvalidEmailAndBlankStrings() throws Exception {
        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"bad-email",
                                  "password":" ",
                                  "fullName":"   ",
                                  "userType":"STUDENT",
                                  "role":"PLAYER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"))
                .andExpect(jsonPath("$.errors.fullName").value("Full name is required"));

        verifyNoInteractions(createCredentialsUseCase, userMapper);
    }

    @Test
    void createCredentialsRejectsUnknownEnumValuesAsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"student@escuelaing.edu.co",
                                  "password":"Password123!",
                                  "fullName":"Ada Lovelace",
                                  "userType":"UNKNOWN_TYPE",
                                  "role":"PLAYER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));

        verifyNoInteractions(createCredentialsUseCase, userMapper);
    }

    @Test
    void createCredentialsRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@escuelaing.edu.co\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));

        verifyNoInteractions(createCredentialsUseCase, userMapper);
    }

    @Test
    void createCredentialsRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));

        verifyNoInteractions(createCredentialsUseCase, userMapper);
    }
}
