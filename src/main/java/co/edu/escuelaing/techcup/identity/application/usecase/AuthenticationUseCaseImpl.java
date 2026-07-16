package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountInactiveException;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountLockedException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidCredentialsException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuthenticationUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.GoogleOAuthPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.OtpUtil;
import co.edu.escuelaing.techcup.identity.shared.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationUseCaseImpl implements AuthenticationUseCase {

    private final UserRepositoryPort userRepository;
    private final OtpRepositoryPort otpRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final EmailPort emailPort;
    private final GoogleOAuthPort googleOAuthPort;
    private final PasswordUtil passwordUtil;
    private final OtpUtil otpUtil;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${auth.max-failed-login-attempts:5}")
    private int maxFailedLoginAttempts;

    @Value("${auth.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    @Override
    public UUID loginWithInstitutionalEmail(String email, String password) {
        log.info("Login attempt with institutional email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        enforceAccountNotLocked(user);

        if (!user.isActive()) {
            auditLoginFailed(user.getId(), "Account inactive");
            throw new AccountInactiveException();
        }

        if (!passwordUtil.matches(password, user.getPassword())) {
            user.registerFailedLoginAttempt(maxFailedLoginAttempts, lockoutDurationMinutes);
            userRepository.save(user);
            auditLoginFailed(user.getId(), "Invalid password");
            throw new InvalidCredentialsException();
        }

        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        }

        sendOtp(user);
        return user.getId();
    }

    @Override
    public UUID loginWithGmail(String googleToken) {
        log.info("Login attempt with Gmail OAuth");

        Map<String, String> googleUserInfo = googleOAuthPort.validateGoogleToken(googleToken);
        String email = googleUserInfo.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        enforceAccountNotLocked(user);

        if (!user.isActive()) {
            auditLoginFailed(user.getId(), "Account inactive");
            throw new AccountInactiveException();
        }

        sendOtp(user);
        return user.getId();
    }

    private void enforceAccountNotLocked(User user) {
        if (user.getStatus() != AccountStatus.LOCKED) {
            return;
        }

        if (user.isLocked()) {
            auditLoginFailed(user.getId(), "Account locked due to multiple failed attempts");
            throw new AccountLockedException(user.getLockedUntil());
        }

        user.resetFailedLoginAttempts();
        userRepository.save(user);
    }

    private void sendOtp(User user) {
        otpRepository.deleteAllByUserId(user.getId());

        String otpCode = otpUtil.generateOtp();
        OtpToken otp = OtpToken.builder()
                .userId(user.getId())
                .code(otpCode)
                .failedAttempts(0)
                .used(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(otpExpirationMinutes))
                .build();

        otpRepository.save(otp);
        emailPort.sendOtp(user.getEmail(), otpCode);

        auditRepository.save(AuditEvent.builder()
                .userId(user.getId())
                .actionType(AuditActionType.OTP_SENT)
                .description("OTP sent for login")
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }

    private void auditLoginFailed(UUID userId, String reason) {
        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.USER_LOGIN_FAILED)
                .description(reason)
                .success(false)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }
}
