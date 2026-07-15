package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuthenticationUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.LogoutUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.OtpUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.PasswordRecoveryUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.TokenValidationUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
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
class AuthControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationUseCase authenticationUseCase;
    @MockBean
    private LogoutUseCase logoutUseCase;
    @MockBean
    private OtpUseCase otpUseCase;
    @MockBean
    private PasswordRecoveryUseCase passwordRecoveryUseCase;
    @MockBean
    private TokenValidationUseCase tokenValidationUseCase;
    @MockBean
    private UserMapper userMapper;

    @Test
    void loginRejectsMalformedJsonBeforeCallingUseCase() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@escuelaing.edu.co\",\"password\":}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));

        verifyNoInteractions(authenticationUseCase);
    }

    @Test
    void loginRejectsEmptyJsonBodyAsMalformedRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));

        verifyNoInteractions(authenticationUseCase);
    }

    @Test
    void loginRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=student@escuelaing.edu.co&password=Password123!"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));

        verifyNoInteractions(authenticationUseCase);
    }

    @Test
    void googleLoginRejectsBlankToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"googleToken\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.googleToken").value("Google token is required"));

        verifyNoInteractions(authenticationUseCase);
    }

    @Test
    void validateOtpRejectsBlankUserIdAndBlankOtpCode() throws Exception {
        mockMvc.perform(post("/api/v1/otp/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"\",\"otpCode\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.userId").value("User ID is required"))
                .andExpect(jsonPath("$.errors.otpCode").value("OTP code is required"));

        verifyNoInteractions(otpUseCase, tokenValidationUseCase);
    }

    @Test
    void validateOtpLetsNonBlankOtpFormatReachDomainValidation() throws Exception {
        when(otpUseCase.validateOtp(TestFixtures.USER_ID, "12A45")).thenReturn(TestFixtures.JWT);
        User user = TestFixtures.activeUser();
        when(tokenValidationUseCase.validateToken(TestFixtures.JWT)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(TestFixtures.userResponse());

        mockMvc.perform(post("/api/v1/otp/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-1\",\"otpCode\":\"12A45\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TestFixtures.JWT));

        verify(otpUseCase).validateOtp(TestFixtures.USER_ID, "12A45");
    }

    @Test
    void resendOtpRejectsBlankUserId() throws Exception {
        mockMvc.perform(post("/api/v1/otp/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.userId").value("User ID is required"));

        verifyNoInteractions(otpUseCase);
    }

    @Test
    void passwordRecoveryRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/password/recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"));

        verifyNoInteractions(passwordRecoveryUseCase);
    }

    @Test
    void passwordResetRejectsMissingRecoveryCodeAndNewPassword() throws Exception {
        mockMvc.perform(post("/api/v1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"student@escuelaing.edu.co\",\"recoveryCode\":\"\",\"newPassword\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.recoveryCode").value("Recovery code is required"))
                .andExpect(jsonPath("$.errors.newPassword").value("New password is required"));

        verifyNoInteractions(passwordRecoveryUseCase);
    }

    @Test
    void validateTokenRejectsMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/v1/token/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_HEADER"))
                .andExpect(jsonPath("$.message").value("Required header is missing: Authorization"));

        verifyNoInteractions(tokenValidationUseCase);
    }

    @Test
    void validateTokenRejectsBasicAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/v1/token/validate")
                        .header("Authorization", "Basic abc123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.message").value("Authorization header must start with Bearer"));

        verifyNoInteractions(tokenValidationUseCase);
    }

    @Test
    void validateTokenRejectsBlankBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/token/validate")
                        .header("Authorization", "Bearer    "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.message").value("Bearer token must not be blank"));

        verifyNoInteractions(tokenValidationUseCase);
    }

    @Test
    void validateTokenTrimsBearerTokenBeforeCallingUseCase() throws Exception {
        User user = TestFixtures.activeUser();
        when(tokenValidationUseCase.validateToken(TestFixtures.JWT)).thenReturn(user);

        mockMvc.perform(post("/api/v1/token/validate")
                        .header("Authorization", "Bearer   " + TestFixtures.JWT + "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(TestFixtures.USER_ID));

        verify(tokenValidationUseCase).validateToken(TestFixtures.JWT);
    }

    @Test
    void logoutRejectsLowercaseBearerPrefix() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "bearer " + TestFixtures.JWT))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));

        verify(logoutUseCase, never()).logout(TestFixtures.JWT);
    }

    @Test
    void logoutRejectsBlankBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));

        verifyNoInteractions(logoutUseCase);
    }

    @Test
    void logoutTrimsBearerTokenBeforeCallingUseCase() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer   " + TestFixtures.JWT + "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session closed successfully"));

        verify(logoutUseCase).logout(TestFixtures.JWT);
    }
}
