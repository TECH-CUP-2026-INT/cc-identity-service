package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountInactiveException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.TokenValidationUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
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
public class TokenValidationUseCaseImpl implements TokenValidationUseCase {

    private final JwtUtil jwtUtil;
    private final UserRepositoryPort userRepository;
    private final RevokedTokenRepositoryPort revokedTokenRepository;
    private final SessionActivityRepositoryPort sessionActivityRepository;
    private final AuditEventRepositoryPort auditRepository;

    @Value("${auth.inactivity-timeout-minutes:30}")
    private int inactivityTimeoutMinutes;

    @Override
    public User validateToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new InvalidTokenException("Token is invalid or expired");
        }

        if (revokedTokenRepository.existsByToken(token)) {
            throw new InvalidTokenException("Token has been revoked");
        }

        enforceSessionActivity(token);

        UUID userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (!user.isActive()) {
            auditRepository.save(AuditEvent.builder()
                    .userId(userId)
                    .actionType(AuditActionType.ACCOUNT_DISABLED)
                    .description("Token rejected — account is no longer active")
                    .success(false)
                    .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                    .build());
            throw new AccountInactiveException();
        }

        return user;
    }

    private void enforceSessionActivity(String token) {
        SessionActivity activity = sessionActivityRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Session not found or already expired"));

        if (activity.isExpiredByInactivity(inactivityTimeoutMinutes)) {
            sessionActivityRepository.deleteByToken(token);
            auditRepository.save(AuditEvent.builder()
                    .userId(activity.getUserId())
                    .actionType(AuditActionType.SESSION_EXPIRED)
                    .description("Session expired due to inactivity")
                    .success(false)
                    .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                    .build());
            throw new InvalidTokenException("Session expired due to inactivity");
        }

        activity.touch();
        sessionActivityRepository.save(activity);
    }
}
