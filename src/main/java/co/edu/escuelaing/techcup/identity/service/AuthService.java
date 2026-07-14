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

/**
 * Registration flows: TC-01 (Student), TC-02 (Guest), TC-03 (Graduate).
 * Auth flows: TC-06/07 (Login), TC-10 (OTP), TC-11 (Refresh).
 */
@Service
public class AuthService {

    private static final String INSTITUTIONAL_DOMAIN       = "@mail.escuelaing.edu.co";
    private static final String INSTITUTIONAL_SECONDDOMAIN = "@escuelaing.edu.co";
    private static final String GMAIL_DOMAIN               = "@gmail.com";

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

    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered.");
        }
        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            throw new BusinessException("ID number already registered.");
        }

        UserEntity user = switch (request.getUserType()) {
            case STUDENT  -> registerStudent(request);
            case GUEST    -> registerGuest(request);
            case GRADUATE -> registerGraduate(request);
        };

        userRepository.save(user);
        otpService.generateAndSend(user);

        return new ApiResponse("Registration successful. Please check your email for the OTP code.", true);
    }

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
        String studentId = req.getAssociatedStudentId();
        userRepository.findByIdAndUserType(studentId, UserType.STUDENT)
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
                .associatedStudentId(studentId)
                .relationship(req.getRelationship())
                .enabled(false)
                .build();
    }

    private UserEntity registerGraduate(RegisterRequest req) {
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

    public ApiResponse verifyOtp(OtpVerifyRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found."));
        otpService.verify(request.getCode(), user);
        user.setEnabled(true);
        userRepository.save(user);
        return new ApiResponse("Account verified successfully.", true);
    }

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
