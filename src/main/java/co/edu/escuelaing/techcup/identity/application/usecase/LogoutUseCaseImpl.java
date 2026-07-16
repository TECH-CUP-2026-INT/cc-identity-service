package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.port.in.LogoutUseCase;
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
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final RevokedTokenRepositoryPort revokedTokenRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final JwtUtil jwtUtil;
    private final SessionActivityRepositoryPort sessionActivityRepository;

    @Override
    public void logout(String token) {
        log.info("Processing logout request");

        // If already revoked, treat as success (idempotent)
        if (revokedTokenRepository.existsByToken(token)) {
            return;
        }

        UUID userId;
        LocalDateTime expiresAt;

        try {
            Claims claims = jwtUtil.extractClaims(token);
            userId = UUID.fromString(claims.getSubject());
            expiresAt = claims.getExpiration().toInstant()
                    .atZone(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            // Token already expired or invalid — treat session as already closed
            log.info("Logout with expired/invalid token — session already closed");
            return;
        }

        revokedTokenRepository.save(RevokedToken.builder()
                .token(token)
                .userId(userId)
                .revokedAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(expiresAt)
                .build());

        sessionActivityRepository.deleteByToken(token);

        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.USER_LOGOUT)
                .description("User logged out — JWT revoked")
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }
}
