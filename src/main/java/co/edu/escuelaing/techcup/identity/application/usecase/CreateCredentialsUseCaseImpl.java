package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class CreateCredentialsUseCaseImpl implements CreateCredentialsUseCase {

    private final UserRepositoryPort userRepository;
    private final OtpRepositoryPort otpRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final EmailPort emailPort;
    private final PasswordUtil passwordUtil;
    private final OtpUtil otpUtil;

    @Value("${otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Override
    public User createCredentials(String email, String password, String fullName,
                                  UserType userType, UserRole role) {
        log.info("Creating credentials for: {} [{}]", email, userType);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordUtil.encode(password))
                .userType(userType)
                .role(role)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        User saved = userRepository.save(user);

        // Send OTP for identity verification
        String otpCode = otpUtil.generateOtp();
        OtpToken otp = OtpToken.builder()
                .userId(saved.getId())
                .code(otpCode)
                .failedAttempts(0)
                .used(false)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(otpExpirationMinutes))
                .build();
        otpRepository.save(otp);
        emailPort.sendOtp(saved.getEmail(), otpCode);

        auditRepository.save(AuditEvent.builder()
                .userId(saved.getId())
                .actionType(AuditActionType.CREDENTIALS_CREATED)
                .description("Credentials created for " + userType.name() + " via inter-service call")
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        return saved;
    }
}
