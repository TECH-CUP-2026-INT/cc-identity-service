package co.edu.escuelaing.techcup.identity.controller;
import co.edu.escuelaing.techcup.identity.dto.ApiResponse;
import co.edu.escuelaing.techcup.identity.dto.AuthResponse;
import co.edu.escuelaing.techcup.identity.dto.LoginRequest;
import co.edu.escuelaing.techcup.identity.dto.OtpVerifyRequest;
import co.edu.escuelaing.techcup.identity.dto.RefreshTokenRequest;
import co.edu.escuelaing.techcup.identity.dto.RegisterRequest;
import co.edu.escuelaing.techcup.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, OTP verification, login, and token refresh.
 * All endpoints are public except where noted — JWT protection is enforced
 * by JwtAuthFilter for any route outside /api/auth/**.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Registration, OTP, login and token refresh")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * SCRUM-13 — registers a new user and sends an OTP to their email.
     */
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse("User registered. Check your email for the OTP.", true));
    }

    /**
     * SCRUM-13 — verifies the OTP and enables the user account.
     */
    @Operation(summary = "Verify OTP code")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(new ApiResponse("Account verified successfully.", true));
    }

    /**
     * SCRUM-14 — authenticates the user and returns access + refresh tokens.
     */
    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * SCRUM-15 — exchanges a valid refresh token for a new access token.
     */
    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}