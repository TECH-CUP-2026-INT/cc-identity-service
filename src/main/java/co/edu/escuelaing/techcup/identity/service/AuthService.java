package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.document.UserDocument.UserType;
import co.edu.escuelaing.techcup.identity.dto.*;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Main service orchestrating authentication and identity flows.
 * Registration flows: TC-01 (Student), TC-02 (Guest), TC-03 (Graduate).
 * Also handles OTP verification, login, token refresh and password recovery.
 * Covers SCRUM-13, SCRUM-14, SCRUM-15, SCRUM-16 and SCRUM-17.
 */
@Service
public class AuthService {

    private static final String INSTITUTIONAL_DOMAIN       = "@mail.escuelaing.edu.co";
    private static final String INSTITUTIONAL_SECONDDOMAIN = "@escuelaing.edu.co";
    private static final String GMAIL_DOMAIN               = "@gmail.com";

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
     * Registers a new user according to their userType (TC-01/02/03) and sends an OTP.
     * Account starts disabled until OTP is verified (SCRUM-13).
     */
    @Transactional
    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered.");
        }
        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException("ID number already registered.");
        }

        UserDocument user = switch (request.getUserType()) {
            case STUDENT  -> registerStudent(request);
            case GUEST    -> registerGuest(request);
            case GRADUATE -> registerGraduate(request);
        };

        userRepository.save(user);
        otpService.generateAndSend(user);

        return new ApiResponse("Registration successful. Please check your email for the OTP code.", true);
    }

    private UserDocument registerStudent(RegisterRequest req) {
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
        return UserDocument.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .userType(UserType.STUDENT)
                .role(UserDocument.Role.PLAYER)
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .academicProgram(req.getAcademicProgram())
                .semester(req.getSemester())
                .enabled(false)
                .build();
    }

    private UserDocument registerGuest(RegisterRequest req) {
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
        String studentId = req.getAssociatedStudentId();
        findStudentById(studentId)
                .orElseThrow(() -> new BusinessException(
                        "The associated student was not found on the platform."));
        return UserDocument.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .userType(UserType.GUEST)
                .role(UserDocument.Role.PLAYER)
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .associatedStudentId(studentId)
                .relationship(req.getRelationship())
                .enabled(false)
                .build();
    }

    private Optional<UserDocument> findStudentById(String studentId) {
        try {
            return userRepository.findByIdAndUserType(UUID.fromString(studentId), UserType.STUDENT);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private UserDocument registerGraduate(RegisterRequest req) {
        boolean hasInstitutional = req.getEmail().endsWith(INSTITUTIONAL_SECONDDOMAIN)
                || req.getEmail().endsWith(INSTITUTIONAL_DOMAIN);
        boolean hasGmail = req.getEmail().endsWith(GMAIL_DOMAIN);
        if (!hasInstitutional && !hasGmail) {
            throw new BusinessException(
                    "Graduates must register with an institutional email or a Gmail address.");
        }
        if (req.getAcademicProgram() == null || req.getAcademicProgram().isBlank()) {
            throw new BusinessException("Academic program is required for graduate registration.");
        }
        return UserDocument.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .userType(UserType.GRADUATE)
                .role(UserDocument.Role.PLAYER)
                .idType(req.getIdType())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .academicProgram(req.getAcademicProgram())
                .enabled(false)
                .build();
    }

    /**
     * Verifies the OTP code and enables the user account (SCRUM-13).
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
