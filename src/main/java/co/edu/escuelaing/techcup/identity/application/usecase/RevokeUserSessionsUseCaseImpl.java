package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.port.in.RevokeUserSessionsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RevokeUserSessionsUseCaseImpl implements RevokeUserSessionsUseCase {

    private final SessionActivityRepositoryPort sessionActivityRepository;
    private final RevokedTokenRepositoryPort revokedTokenRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void revokeAllSessions(UUID userId) {
        var sessions = sessionActivityRepository.findAllByUserId(userId);
        log.info("Revoking {} active session(s) for user {}", sessions.size(), userId);

        for (SessionActivity session : sessions) {
            revokeSession(userId, session.getToken());
        }

        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.ACCOUNT_DISABLED)
                .description("All sessions revoked — account disabled in users-players-service")
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }

    private void revokeSession(UUID userId, String token) {
        if (revokedTokenRepository.existsByToken(token)) {
            sessionActivityRepository.deleteByToken(token);
            return;
        }

        LocalDateTime expiresAt;
        try {
            Claims claims = jwtUtil.extractClaims(token);
            expiresAt = claims.getExpiration().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            sessionActivityRepository.deleteByToken(token);
            return;
        }

        revokedTokenRepository.save(RevokedToken.builder()
                .token(token)
                .userId(userId)
                .revokedAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(expiresAt)
                .build());

        sessionActivityRepository.deleteByToken(token);
    }
}
