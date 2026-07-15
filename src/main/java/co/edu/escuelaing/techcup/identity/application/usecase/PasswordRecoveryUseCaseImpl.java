package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.RecoveryCodeExpiredException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.PasswordRecoveryUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RecoveryTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.OtpUtil;
import co.edu.escuelaing.techcup.identity.shared.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@RequiredArgsConstructor
@Service
public class PasswordRecoveryUseCaseImpl implements PasswordRecoveryUseCase {

    private final UserRepositoryPort userRepository;
    private final RecoveryTokenRepositoryPort recoveryTokenRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final EmailPort emailPort;
    private final OtpUtil otpUtil;
    private final PasswordUtil passwordUtil;

    @Value("${recovery.expiration-minutes:15}")
    private int recoveryExpirationMinutes;

    @Override
    public void requestRecovery(String email) {
        log.info("Password recovery requested for email: {}", email);

        // TC-09: Does NOT reveal whether the email exists (security measure)
        userRepository.findByEmail(email).ifPresent(user -> {
            recoveryTokenRepository.deleteAllByUserId(user.getId());

            String code = otpUtil.generateRecoveryCode();
            RecoveryToken token = RecoveryToken.builder()
                    .userId(user.getId())
                    .code(code)
                    .used(false)
                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                    .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(recoveryExpirationMinutes))
                    .build();

            recoveryTokenRepository.save(token);
            emailPort.sendRecoveryCode(user.getEmail(), code);

            auditRepository.save(AuditEvent.builder()
                    .userId(user.getId())
                    .actionType(AuditActionType.PASSWORD_RECOVERY_REQUESTED)
                    .description("Password recovery requested")
                    .success(true)
                    .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                    .build());
        });
    }

    @Override
    public void resetPassword(String email, String recoveryCode, String newPassword) {
        log.info("Password reset attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("Invalid recovery request"));

        RecoveryToken token = recoveryTokenRepository.findLatestByUserId(user.getId())
                .orElseThrow(() -> new InvalidTokenException("No recovery code found"));

        if (token.isExpired()) {
            throw new RecoveryCodeExpiredException();
        }

        if (token.isUsed() || !token.getCode().equals(recoveryCode)) {
            throw new InvalidTokenException("Invalid recovery code");
        }

        token.markAsUsed();
        recoveryTokenRepository.save(token);

        user.setPassword(passwordUtil.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        auditRepository.save(AuditEvent.builder()
                .userId(user.getId())
                .actionType(AuditActionType.PASSWORD_RESET)
                .description("Password successfully reset")
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }
}
