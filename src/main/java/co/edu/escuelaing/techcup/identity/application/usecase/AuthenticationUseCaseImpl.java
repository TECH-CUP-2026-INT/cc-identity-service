package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountInactiveException;
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
import java.util.Map;

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

    @Override
    public String loginWithInstitutionalEmail(String email, String password) {
        log.info("Login attempt with institutional email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            auditLoginFailed(user.getId(), "Account inactive");
            throw new AccountInactiveException();
        }

        if (!passwordUtil.matches(password, user.getPassword())) {
            auditLoginFailed(user.getId(), "Invalid password");
            throw new InvalidCredentialsException();
        }

        sendOtp(user);
        return user.getId();
    }

    @Override
    public String loginWithGmail(String googleToken) {
        log.info("Login attempt with Gmail OAuth");

        Map<String, String> googleUserInfo = googleOAuthPort.validateGoogleToken(googleToken);
        String email = googleUserInfo.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!user.isActive()) {
            auditLoginFailed(user.getId(), "Account inactive");
            throw new AccountInactiveException();
        }

        sendOtp(user);
        return user.getId();
    }

    private void sendOtp(User user) {
        otpRepository.deleteAllByUserId(user.getId());

        String otpCode = otpUtil.generateOtp();
        OtpToken otp = OtpToken.builder()
                .userId(user.getId())
                .code(otpCode)
                .failedAttempts(0)
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .build();

        otpRepository.save(otp);
        emailPort.sendOtp(user.getEmail(), otpCode);

        auditRepository.save(AuditEvent.builder()
                .userId(user.getId())
                .actionType(AuditActionType.OTP_SENT)
                .description("OTP sent for login")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void auditLoginFailed(String userId, String reason) {
        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.USER_LOGIN_FAILED)
                .description(reason)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
