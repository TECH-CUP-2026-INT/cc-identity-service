package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.dto.AuthResponse;
import co.edu.escuelaing.techcup.identity.dto.GmailOtpVerifyRequest;
import co.edu.escuelaing.techcup.identity.service.GmailLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Gmail OAuth2 login flow (TC-07).
 *
 * Phase 1 is handled automatically by Spring Security OAuth2 at /oauth2/authorization/google.
 * Phase 2 is exposed here: the client submits the OTP to receive JWT tokens.
 */
@RestController
@RequestMapping("/api/auth/gmail")
@Tag(name = "Gmail Login", description = "Two-phase Gmail OAuth2 login with OTP verification")
public class GmailAuthController {

    private final GmailLoginService gmailLoginService;

    public GmailAuthController(GmailLoginService gmailLoginService) {
        this.gmailLoginService = gmailLoginService;
    }

    /**
     * TC-07 Phase 2 — verifies the OTP sent after Google authentication
     * and returns JWT access + refresh tokens.
     */
    @Operation(summary = "Verify OTP after Gmail login and receive JWT tokens")
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody GmailOtpVerifyRequest request) {
        AuthResponse response = gmailLoginService.completeLogin(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }
}
