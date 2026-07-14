package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.dto.*;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main service orchestrating authentication and identity flows.
 * Handles user registration, OTP verification, login, and token refresh.
 * Covers SCRUM-13, SCRUM-14, SCRUM-15, SCRUM-16 and SCRUM-17.
 */

@Service
public class AuthService { 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, OtpService otpService, AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registers a new user and sends an OTP to their email.
     * Account starts disabled until OTP is verified (SCRUM-13).
     * @param request registration data
     * @return confirmation message
     * 
     */
    @Transactional
    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        UserDocument user = UserDocument.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(false)
                .role(UserDocument.Role.USER)
                .build();

        userRepository.save(user);
        otpService.generateAndSend(user);

        return new ApiResponse("Registration successful. Please check your email for the OTP code.", true);
    }

    /**
     * Verifies the OTP code and enables the user account (SCRUM-13).
     * @param request email and OTP code
     * @return confirmation message
     */
    @Transactional
    public ApiResponse verifyOtp(OtpVerifyRequest request) {
        UserDocument user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.verify(request.getCode(), user);

        user.setEnabled(true);
        userRepository.save(user);

        return new ApiResponse("Account verified successfully.", true);
    }

    /**
     * Authenticates a user and returns JWT access and refresh tokens (SCRUM-14).
     * @param request login credentials
     * @return access token, refresh token, email and role
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        UserDocument user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    /**
     * Validates a refresh token and issues a new access token (SCRUM-15).
     * @param request refresh token
     * @return new access token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtService.extractEmail(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return new AuthResponse(newAccessToken, request.getRefreshToken(), user.getEmail(), user.getRole().name());
    }

    /**
     * Sends a password recovery OTP when the account exists.
     * The response remains generic to avoid revealing registered emails (SCRUM-16).
     */
    @Transactional
    public ApiResponse requestPasswordRecovery(PasswordRecoveryRequest request) {
        userRepository.findByEmail(request.getEmail())
                .filter(UserDocument::isEnabled)
                .ifPresent(otpService::generateAndSend);

        return new ApiResponse(
                "If the email is registered, a recovery code has been sent.",
                true
        );
    }

    /**
     * Validates the recovery OTP and updates the encrypted password (SCRUM-16).
     */
    @Transactional
    public ApiResponse resetPassword(PasswordResetRequest request) {
        UserDocument user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid or expired recovery request"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Invalid or expired recovery request");
        }

        otpService.verify(request.getCode(), user);

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse("Password updated successfully.", true);
    }
}
