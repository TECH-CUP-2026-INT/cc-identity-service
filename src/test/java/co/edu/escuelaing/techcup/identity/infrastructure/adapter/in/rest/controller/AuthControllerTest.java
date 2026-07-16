package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidCredentialsException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuthenticationUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.LogoutUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.OtpUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.PasswordRecoveryUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.TokenValidationUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.GoogleLoginRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.LoginRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.OtpResendRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.OtpValidationRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.PasswordRecoveryRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.PasswordResetRequest;
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

import static org.mockito.Mockito.verify;
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
class AuthControllerTest {

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
    void loginReturnsUserIdAndOtpMessage() throws Exception {
        when(authenticationUseCase.loginWithInstitutionalEmail(TestFixtures.EMAIL, TestFixtures.PASSWORD))
                .thenReturn(TestFixtures.USER_ID);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(TestFixtures.EMAIL)
                                .password(TestFixtures.PASSWORD)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TestFixtures.USER_ID.toString()))
                .andExpect(jsonPath("$.message").value("OTP sent to your email. Please validate to complete login."));
    }

    @Test
    void loginValidationErrorsReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email("not-an-email")
                                .password("")
                                .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginUseCaseExceptionIsMappedByGlobalHandler() throws Exception {
        when(authenticationUseCase.loginWithInstitutionalEmail(TestFixtures.EMAIL, "wrong"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(LoginRequest.builder()
                                .email(TestFixtures.EMAIL)
                                .password("wrong")
                                .build())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void googleLoginReturnsUserIdAndOtpMessage() throws Exception {
        when(authenticationUseCase.loginWithGmail("google-token")).thenReturn(TestFixtures.USER_ID);

        mockMvc.perform(post("/api/v1/auth/login/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GoogleLoginRequest.builder()
                                .googleToken("google-token")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TestFixtures.USER_ID.toString()));
    }

    @Test
    void validateOtpReturnsJwtAndMappedUser() throws Exception {
        User user = TestFixtures.activeUser();
        when(otpUseCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE)).thenReturn(TestFixtures.JWT);
        when(tokenValidationUseCase.validateToken(TestFixtures.JWT)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(TestFixtures.userResponse());

        mockMvc.perform(post("/api/v1/otp/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OtpValidationRequest.builder()
                                .userId(TestFixtures.USER_ID)
                                .otpCode(TestFixtures.OTP_CODE)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TestFixtures.JWT))
                .andExpect(jsonPath("$.user.id").value(TestFixtures.USER_ID.toString()))
                .andExpect(jsonPath("$.user.email").value(TestFixtures.EMAIL));
    }

    @Test
    void resendOtpReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/v1/otp/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OtpResendRequest.builder()
                                .userId(TestFixtures.USER_ID)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP resent successfully"));

        verify(otpUseCase).resendOtp(TestFixtures.USER_ID);
    }

    @Test
    void requestRecoveryReturnsNeutralSecurityMessage() throws Exception {
        mockMvc.perform(post("/api/v1/password/recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PasswordRecoveryRequest.builder()
                                .email(TestFixtures.EMAIL)
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a recovery code has been sent."));

        verify(passwordRecoveryUseCase).requestRecovery(TestFixtures.EMAIL);
    }

    @Test
    void resetPasswordReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/v1/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PasswordResetRequest.builder()
                                .email(TestFixtures.EMAIL)
                                .recoveryCode("ABCD1234")
                                .newPassword("NewPassword123!")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        verify(passwordRecoveryUseCase).resetPassword(TestFixtures.EMAIL, "ABCD1234", "NewPassword123!");
    }

    @Test
    void validateTokenStripsBearerHeaderAndReturnsUserData() throws Exception {
        User user = TestFixtures.activeUser();
        when(tokenValidationUseCase.validateToken(TestFixtures.JWT)).thenReturn(user);

        mockMvc.perform(post("/api/v1/token/validate")
                        .header("Authorization", "Bearer " + TestFixtures.JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(TestFixtures.USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(TestFixtures.EMAIL))
                .andExpect(jsonPath("$.role").value(user.getRole().name()));
    }

    @Test
    void logoutStripsBearerHeaderAndReturnsSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + TestFixtures.JWT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session closed successfully"));

        verify(logoutUseCase).logout(TestFixtures.JWT);
    }
}
