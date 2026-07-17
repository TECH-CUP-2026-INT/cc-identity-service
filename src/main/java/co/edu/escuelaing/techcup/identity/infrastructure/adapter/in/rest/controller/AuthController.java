package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Authentication endpoints: login, OTP verification, password recovery, JWT validation, and logout. " +
        "Covers institutional login, Google OAuth 2.0 login, JWT validation, " +
        "password recovery, OTP verification, JWT inactivity expiration, and logout.")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;
    private final LogoutUseCase logoutUseCase;
    private final OtpUseCase otpUseCase;
    private final PasswordRecoveryUseCase passwordRecoveryUseCase;
    private final TokenValidationUseCase tokenValidationUseCase;
    private final UserMapper userMapper;

    @PostMapping("/auth/login")
    @Operation(
            summary = "Login with institutional email and password",
            description = "Authenticates the user with their institutional email (@escuelaing.edu.co) and password. " +
                    "If credentials are valid, sends an OTP code to the user's email. " +
                    "Login is NOT complete until the OTP is verified via POST /otp/validate. " +
                    "Logs USER_LOGIN audit event. The account is temporarily locked (default 15 minutes) " +
                    "after reaching the configured max failed attempts (default 5), and unlocks automatically " +
                    "when that period expires."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valid credentials. OTP sent to user's email."),
            @ApiResponse(responseCode = "400", description = "Invalid input (empty email, wrong format, malformed JSON)."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials (email or password don't match)."),
            @ApiResponse(responseCode = "403", description = "Inactive account, or temporarily locked due to multiple failed attempts.")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UUID userId = authenticationUseCase.loginWithInstitutionalEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(LoginResponse.builder()
                .userId(userId)
                .message("OTP sent to your email. Please validate to complete login.")
                .build());
    }

    @PostMapping("/auth/login/google")
    @Operation(
            summary = "Login with Google OAuth 2.0",
            description = "Authenticates the user via a Google OAuth 2.0 token (obtained through Google's consent flow). " +
                    "Intended for guests, referees, alumni without active institutional email, and organizers. " +
                    "The user must already exist in the system (created via the corresponding registration flow); " +
                    "this endpoint does NOT create new accounts. After successful authentication, an OTP is sent to the email. " +
                    "Logs OTP_SENT audit event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valid Google token. OTP sent to user's email."),
            @ApiResponse(responseCode = "400", description = "Empty or invalid Google token, or non-institutional email domain."),
            @ApiResponse(responseCode = "403", description = "Blocked or inactive account.")
    })
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        UUID userId = authenticationUseCase.loginWithGmail(request.getGoogleToken());
        return ResponseEntity.ok(LoginResponse.builder()
                .userId(userId)
                .message("OTP sent to your email. Please validate to complete login.")
                .build());
    }

    @PostMapping("/otp/validate")
    @Operation(
            summary = "Verify OTP code and get JWT token",
            description = "Validates the 6-digit OTP code sent to the user's email during login. " +
                    "If the OTP is correct and has not expired (configurable, default 5 minutes), generates and returns a JWT token " +
                    "along with the authenticated user's data. The OTP has a max number of attempts (default 3); " +
                    "if exceeded, the user must request a new OTP. This is the final step of the two-factor authentication flow."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valid OTP. Returns JWT and authenticated user data."),
            @ApiResponse(responseCode = "400", description = "Incorrect or expired OTP, or max attempts reached.")
    })
    public ResponseEntity<OtpResponse> validateOtp(@Valid @RequestBody OtpValidationRequest request) {
        String jwt = otpUseCase.validateOtp(request.getUserId(), request.getOtpCode());
        User user = tokenValidationUseCase.validateToken(jwt);
        return ResponseEntity.ok(OtpResponse.builder()
                .token(jwt)
                .user(userMapper.toResponse(user))
                .build());
    }

    @PostMapping("/otp/resend")
    @Operation(
            summary = "Resend OTP code",
            description = "Generates and sends a new OTP code to the user's email, invalidating any previous OTP. " +
                    "Has a configurable cooldown (default 60 seconds) between resends to prevent abuse. " +
                    "The new OTP has the same expiration duration as the original."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New OTP sent successfully."),
            @ApiResponse(responseCode = "400", description = "Active cooldown or invalid userId."),
            @ApiResponse(responseCode = "404", description = "User not found.")
    })
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody OtpResendRequest request) {
        otpUseCase.resendOtp(request.getUserId());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("OTP resent successfully")
                .build());
    }

    @PostMapping("/password/recovery")
    @Operation(
            summary = "Request password recovery code",
            description = "Sends a single-use recovery code to the provided institutional email. " +
                    "The code has a configurable expiration time (default 15 minutes). " +
                    "For security, the response is always 200 OK regardless of whether the email exists in the system. " +
                    "Logs PASSWORD_RECOVERY_REQUEST audit event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the email exists, a recovery code was sent. Generic response for security."),
            @ApiResponse(responseCode = "400", description = "Empty or invalid email format.")
    })
    public ResponseEntity<MessageResponse> requestRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        passwordRecoveryUseCase.requestRecovery(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("If the email exists, a recovery code has been sent.")
                .build());
    }

    @PostMapping("/password/reset")
    @Operation(
            summary = "Reset password with recovery code",
            description = "Resets the user's password using the recovery code received by email. " +
                    "The code is single-use and has an expiration time (default 15 minutes). " +
                    "The new password is stored with BCrypt hash. " +
                    "Logs PASSWORD_RESET audit event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid recovery code, expired, or incomplete input data."),
            @ApiResponse(responseCode = "410", description = "Recovery code expired (Gone).")
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryUseCase.resetPassword(request.getEmail(), request.getRecoveryCode(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password reset successfully")
                .build());
    }

    @PostMapping("/token/validate")
    @Operation(
            summary = "Validate JWT token",
            description = "Validates a JWT token sent in the Authorization header (format: 'Bearer <token>'). " +
                    "Verifies the signature, absolute expiration, that it hasn't been revoked (logout), and that the session hasn't " +
                    "expired due to inactivity (JWT inactivity expiration: default 30 minutes without activity). Each valid call renews " +
                    "the inactivity window. If valid, returns user data: id, email, and role. " +
                    "This endpoint is consumed by other microservices to verify user authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valid token. Returns authenticated user data."),
            @ApiResponse(responseCode = "400", description = "Missing Authorization header."),
            @ApiResponse(responseCode = "401", description = "Invalid, expired, or revoked token; inactivity session expired; or header without 'Bearer' prefix.")
    })
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Parameter(description = "Authorization header in 'Bearer <JWT>' format", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        User user = tokenValidationUseCase.validateToken(token);
        return ResponseEntity.ok(TokenValidationResponse.builder()
                .valid(true)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/auth/logout")
    @Operation(
            summary = "Logout and revoke JWT token",
            description = "Logs out the user by revoking their JWT token. The token is added to a revocation list in MongoDB " +
                    "with a TTL index that automatically removes it upon expiration. Any attempt to use a revoked token " +
                    "will be rejected by the JWT security filter. Logs USER_LOGOUT audit event."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session closed and token revoked successfully."),
            @ApiResponse(responseCode = "401", description = "Authorization header without 'Bearer' prefix, empty token, or invalid token.")
    })
    public ResponseEntity<MessageResponse> logout(
            @Parameter(description = "Authorization header in 'Bearer <JWT>' format", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        logoutUseCase.logout(token);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Session closed successfully")
                .build());
    }

    private String extractBearerToken(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Authorization header must start with Bearer");
        }
        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            throw new InvalidTokenException("Bearer token must not be blank");
        }
        return token;
    }
}
