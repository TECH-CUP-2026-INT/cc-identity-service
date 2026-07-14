package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.*;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.entity.UserEntity.UserType;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Main service orchestrating authentication and identity flows.
 *
 * Registration flows covered:
 *   TC-01 — Student registration (institutional email required)
 *   TC-02 — Guest registration   (Gmail required; associatedStudentId validated against DB)
 *   TC-03 — Graduate registration(institutional or Gmail accepted)
 *
 * Authentication flows covered:
 *   TC-06 — Login with institutional email
 *   TC-07 — Login with Gmail (same endpoint; email type resolved at registration)
 *   TC-08 — JWT validation (handled by JwtAuthFilter)
 *   TC-10 — OTP two-factor validation
 *   TC-11 — Automatic session expiration (handled by JwtService TTL)
 */
@Service
public class AuthService {

    // Domain constants — kept here to avoid magic strings scattered across the code
    private static final String INSTITUTIONAL_DOMAIN = "@mail.escuelaing.edu.co";
    private static final String INSTITUTIONAL_SECONDDOMAIN = "@escuelaing.edu.co";
    private static final String GMAIL_DOMAIN         = "@gmail.com";

    private final UserRepository         userRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;
    private final OtpService             otpService;
    private final AuthenticationManager  authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       OtpService otpService,
                       AuthenticationManager authenticationManager,
                       UserDetailsServiceImpl userDetailsService) {
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtService           = jwtService;
        this.otpService           = otpService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService   = userDetailsService;
    }

    // ── TC-01 / TC-02 / TC-03 ─────────────────────────────────────────────

    /**
     * Registers a new user according to their userType.
     * Validates email domain, required fields, and (for GUEST) the associated student.
     * Account starts disabled until OTP is verified (TC-10).
     *
     * @param request registration data including userType and type-specific fields
     * @return confirmation message
     */
    @Transactional
    public ApiResponse register(RegisterRequest request) {

        // Common validations
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered.");
        }
        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException("ID number already registered.");
        }

        // Route to the correct registration flow
        UserEntity user = switch (request.getUserType()) {
            case STUDENT  -> registerStudent(request);
            case GUEST    -> registerGuest(request);
            case GRADUATE -> registerGraduate(request);
        };

        userRepository.save(user);
        otpService.generateAndSend(user);

        return new ApiResponse("Registration successful. Please check your email for the OTP code.", true);
    }

    // ── TC-01 Student ─────────────────────────────────────────────────────

    /**
     * Builds a STUDENT user entity.
     * Rules (TC-01):
     *   - Institutional email (@mail.escuelaing.edu.co) is mandatory.
     *   - academicProgram and semester are mandatory.
     *   - Role defaults to PLAYER.
     */
    private UserEntity registerStudent(RegisterRequest req) {
        if (!req.getEmail().endsWith(INSTITUTIONAL_DOMAIN)) {
            throw new BusinessException(
                    "Students must register with an institutional email (@mail.escuelaing.edu.co).");
        }
        if (req.getAcademicProgram() == null || req.getAcademicProgram().isBlank()) {
            throw new BusinessException("Academic program is required for student registration.");
        }
        if (req.getSemester() == null) {
            throw new BusinessException("Semester is required for student registration.");
        }

        return UserEntity.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .userType(UserType.STUDENT)
                .role(UserEntity.Role.PLAYER)
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .academicProgram(req.getAcademicProgram())
                .semester(req.getSemester())
                .enabled(false)
                .build();
    }

    // ── TC-02 Guest ───────────────────────────────────────────────────────

    /**
     * Builds a GUEST user entity.
     * Rules (TC-02):
     *   - Gmail (@gmail.com) is mandatory.
     *   - associatedStudentId must reference an existing STUDENT in the database.
     *   - relationship is mandatory.
     *   - Role defaults to PLAYER.
     */
    private UserEntity registerGuest(RegisterRequest req) {
        if (!req.getEmail().endsWith(GMAIL_DOMAIN)) {
            throw new BusinessException(
                    "Guests (family/friends) must register with a Gmail address (@gmail.com).");
        }
        if (req.getAssociatedStudentId() == null) {
            throw new BusinessException("Guests must declare the associated student ID.");
        }
        if (req.getRelationship() == null || req.getRelationship().isBlank()) {
            throw new BusinessException("Relationship to the associated student is required.");
        }

        // Validate the associated student exists and is actually a STUDENT
        userRepository.findByIdAndUserType(req.getAssociatedStudentId(), UserType.STUDENT)
                .orElseThrow(() -> new BusinessException(
                        "The associated student was not found on the platform."));

        return UserEntity.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .userType(UserType.GUEST)
                .role(UserEntity.Role.PLAYER)
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .associatedStudentId(req.getAssociatedStudentId())
                .relationship(req.getRelationship())
                .enabled(false)
                .build();
    }

    // ── TC-03 Graduate ────────────────────────────────────────────────────

    /**
     * Builds a GRADUATE user entity.
     * Rules (TC-03):
     *   - Institutional email preferred; Gmail accepted if institutional is not available.
     *   - No student association required.
     *   - academicProgram is mandatory.
     *   - Role defaults to PLAYER.
     */
    private UserEntity registerGraduate(RegisterRequest req) {
        boolean hasInstitutional = req.getEmail().endsWith(INSTITUTIONAL_SECONDDOMAIN)|| req.getEmail().endsWith(INSTITUTIONAL_DOMAIN);
        boolean hasGmail         = req.getEmail().endsWith(GMAIL_DOMAIN);

        if (!hasInstitutional && !hasGmail) {
            throw new BusinessException(
                    "Graduates must register with an institutional email or a Gmail address.");
        }
        if (req.getAcademicProgram() == null || req.getAcademicProgram().isBlank()) {
            throw new BusinessException("Academic program is required for graduate registration.");
        }

        return UserEntity.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .userType(UserType.GRADUATE)
                .role(UserEntity.Role.PLAYER)
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .academicProgram(req.getAcademicProgram())
                .enabled(false)
                .build();
    }

    // ── TC-10 OTP Verification ────────────────────────────────────────────

    /**
     * Verifies the OTP code and enables the user account (TC-10).
     *
     * @param request email and OTP code
     * @return confirmation message
     */
    @Transactional
    public ApiResponse verifyOtp(OtpVerifyRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found."));

        otpService.verify(request.getCode(), user);

        user.setEnabled(true);
        userRepository.save(user);

        return new ApiResponse("Account verified successfully.", true);
    }

    // ── TC-06 / TC-07 Login ───────────────────────────────────────────────

    /**
     * Authenticates a user and returns JWT access and refresh tokens.
     * Handles both institutional (TC-06) and Gmail (TC-07) logins —
     * the email type was resolved at registration time.
     *
     * @param request login credentials
     * @return access token, refresh token, email and role
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found."));

        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    // ── TC-11 Token Refresh ───────────────────────────────────────────────

    /**
     * Validates a refresh token and issues a new access token.
     *
     * @param request refresh token
     * @return new access token paired with the existing refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtService.extractEmail(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new BusinessException("Invalid or expired refresh token.");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found."));

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return new AuthResponse(newAccessToken, request.getRefreshToken(), user.getEmail(), user.getRole().name());
    }
}