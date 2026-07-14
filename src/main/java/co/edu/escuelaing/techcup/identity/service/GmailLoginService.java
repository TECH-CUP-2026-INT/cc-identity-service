package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.AuthResponse;
import co.edu.escuelaing.techcup.identity.entity.AuditEventType;
import co.edu.escuelaing.techcup.identity.entity.AuditResult;
import co.edu.escuelaing.techcup.identity.entity.OtpPurpose;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Handles the two-phase Gmail login flow (TC-07).
 */
@Service
public class GmailLoginService {

    private static final Set<UserEntity.Role> ALLOWED_ROLES =
            Set.of(UserEntity.Role.USER, UserEntity.Role.REFEREE,
                   UserEntity.Role.ORGANIZER, UserEntity.Role.ADMIN);

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuditService auditService;

    public GmailLoginService(UserRepository userRepository,
                             OtpService otpService,
                             JwtService jwtService,
                             UserDetailsServiceImpl userDetailsService,
                             AuditService auditService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @Transactional
    public void initiateLogin(String googleEmail) {
        UserEntity user = userRepository.findByEmail(googleEmail)
                .orElseThrow(() -> {
                    auditService.record(AuditEventType.GMAIL_LOGIN_FAILED, AuditResult.FAILURE,
                            null, googleEmail, "Gmail login failed — email not registered", null);
                    return new RuntimeException("Email not registered. Please sign up first.");
                });

        if (!user.isEnabled()) {
            auditService.record(AuditEventType.GMAIL_LOGIN_FAILED, AuditResult.FAILURE,
                    user.getId(), googleEmail, "Gmail login failed — account disabled", null);
            throw new RuntimeException("Account is disabled. Please verify your email first.");
        }

        if (!ALLOWED_ROLES.contains(user.getRole())) {
            auditService.record(AuditEventType.GMAIL_LOGIN_FAILED, AuditResult.FAILURE,
                    user.getId(), googleEmail, "Gmail login failed — role not authorized", null);
            throw new RuntimeException("Role not authorized for Gmail login.");
        }

        otpService.generateAndSend(user, OtpPurpose.GMAIL_LOGIN);

        auditService.record(AuditEventType.GMAIL_LOGIN_STARTED, AuditResult.SUCCESS,
                user.getId(), googleEmail, "Gmail login started — OTP sent", null);
        auditService.record(AuditEventType.OTP_SENT, AuditResult.SUCCESS,
                user.getId(), googleEmail, "OTP sent for Gmail login", null);
    }

    @Transactional
    public AuthResponse completeLogin(String email, String code) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled.");
        }

        try {
            otpService.verify(code, user, OtpPurpose.GMAIL_LOGIN);
        } catch (RuntimeException ex) {
            auditService.record(AuditEventType.GMAIL_LOGIN_FAILED, AuditResult.FAILURE,
                    user.getId(), email, "Gmail login failed — invalid OTP", null);
            throw ex;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        auditService.record(AuditEventType.GMAIL_LOGIN_SUCCESS, AuditResult.SUCCESS,
                user.getId(), email, "Gmail login completed successfully", null);
        auditService.record(AuditEventType.OTP_VERIFIED, AuditResult.SUCCESS,
                user.getId(), email, "OTP verified for Gmail login", null);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }
}
