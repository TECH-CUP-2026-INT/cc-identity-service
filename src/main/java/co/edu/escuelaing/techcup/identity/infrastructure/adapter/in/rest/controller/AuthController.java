package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

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
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.OtpResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.TokenValidationResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Authentication, OTP, password recovery, token validation and logout (TC-06 to TC-11, TC-29)")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;
    private final LogoutUseCase logoutUseCase;
    private final OtpUseCase otpUseCase;
    private final PasswordRecoveryUseCase passwordRecoveryUseCase;
    private final TokenValidationUseCase tokenValidationUseCase;
    private final UserMapper userMapper;

    @PostMapping("/auth/login")
    @Operation(summary = "TC-06: Login with institutional email and password")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String userId = authenticationUseCase.loginWithInstitutionalEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(LoginResponse.builder()
                .userId(userId)
                .message("OTP sent to your email. Please validate to complete login.")
                .build());
    }

    @PostMapping("/auth/login/google")
    @Operation(summary = "TC-07: Login with Google OAuth 2.0")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        String userId = authenticationUseCase.loginWithGmail(request.getGoogleToken());
        return ResponseEntity.ok(LoginResponse.builder()
                .userId(userId)
                .message("OTP sent to your email. Please validate to complete login.")
                .build());
    }

    @PostMapping("/otp/validate")
    @Operation(summary = "TC-10: Validate OTP and obtain JWT token")
    public ResponseEntity<OtpResponse> validateOtp(@Valid @RequestBody OtpValidationRequest request) {
        String jwt = otpUseCase.validateOtp(request.getUserId(), request.getOtpCode());
        User user = tokenValidationUseCase.validateToken(jwt);
        return ResponseEntity.ok(OtpResponse.builder()
                .token(jwt)
                .user(userMapper.toResponse(user))
                .build());
    }

    @PostMapping("/otp/resend")
    @Operation(summary = "TC-10: Resend OTP code")
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody OtpResendRequest request) {
        otpUseCase.resendOtp(request.getUserId());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("OTP resent successfully")
                .build());
    }

    @PostMapping("/password/recovery")
    @Operation(summary = "TC-09: Request password recovery code")
    public ResponseEntity<MessageResponse> requestRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        passwordRecoveryUseCase.requestRecovery(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("If the email exists, a recovery code has been sent.")
                .build());
    }

    @PostMapping("/password/reset")
    @Operation(summary = "TC-09: Reset password with recovery code")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryUseCase.resetPassword(request.getEmail(), request.getRecoveryCode(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password reset successfully")
                .build());
    }

    @PostMapping("/token/validate")
    @Operation(summary = "TC-08: Validate JWT token (also enforces TC-11 expiration)")
    public ResponseEntity<TokenValidationResponse> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        User user = tokenValidationUseCase.validateToken(token);
        return ResponseEntity.ok(TokenValidationResponse.builder()
                .valid(true)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "TC-29: Logout and revoke JWT token")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        logoutUseCase.logout(token);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Session closed successfully")
                .build());
    }
}
