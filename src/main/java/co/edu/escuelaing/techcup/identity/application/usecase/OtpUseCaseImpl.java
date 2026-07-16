package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountBlockedException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidOtpException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.OtpUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import co.edu.escuelaing.techcup.identity.shared.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class OtpUseCaseImpl implements OtpUseCase {

    private final OtpRepositoryPort otpRepository;
    private final UserRepositoryPort userRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final EmailPort emailPort;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final SessionActivityRepositoryPort sessionActivityRepository;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Override
    public String validateOtp(UUID userId, String otpCode) {
        log.info("Validating OTP for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        OtpToken otp = otpRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new InvalidOtpException("No OTP found. Please request a new one."));

        if (otp.getFailedAttempts() >= maxAttempts) {
            auditOtpFailed(userId, "Account blocked - max attempts exceeded");
            throw new AccountBlockedException();
        }

        if (otp.isExpired()) {
            auditOtpFailed(userId, "OTP expired");
            throw new InvalidOtpException("OTP has expired. Please request a new one.");
        }

        if (otp.isUsed()) {
            throw new InvalidOtpException("OTP has already been used. Please request a new one.");
        }

        if (!otp.getCode().equals(otpCode)) {
            otp.incrementFailedAttempts();
            otpRepository.save(otp);
            auditOtpFailed(userId, "Incorrect OTP code");

            if (otp.getFailedAttempts() >= maxAttempts) {
                throw new AccountBlockedException();
            }
            throw new InvalidOtpException("Incorrect OTP code. Attempts remaining: " +
                    (maxAttempts - otp.getFailedAttempts()));
        }

        otp.markAsUsed();
        otpRepository.save(otp);

        String jwt = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        sessionActivityRepository.save(SessionActivity.builder()
                .token(jwt)
                .userId(user.getId())
                .lastActivityAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.USER_LOGIN)
                .description("Successful login after OTP validation")
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        return jwt;
    }

    @Override
    public void resendOtp(UUID userId) {
        log.info("Resending OTP for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        OtpToken existingOtp = otpRepository.findLatestByUserId(userId).orElse(null);
        if (existingOtp != null) {
            long secondsSinceCreation = java.time.Duration.between(
                    existingOtp.getCreatedAt().toInstant(ZoneOffset.UTC),
                    LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC)).getSeconds();
            if (secondsSinceCreation < resendCooldownSeconds) {
                throw new InvalidOtpException("Please wait " +
                        (resendCooldownSeconds - secondsSinceCreation) + " seconds before requesting a new OTP");
            }
        }

        otpRepository.deleteAllByUserId(userId);

        String otpCode = otpUtil.generateOtp();
        OtpToken newOtp = OtpToken.builder()
                .userId(userId)
                .code(otpCode)
                .failedAttempts(0)
                .used(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(otpExpirationMinutes))
                .build();

        otpRepository.save(newOtp);
        emailPort.sendOtp(user.getEmail(), otpCode);
    }

    private void auditOtpFailed(UUID userId, String reason) {
        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.OTP_FAILED)
                .description(reason)
                .success(false)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }
}
