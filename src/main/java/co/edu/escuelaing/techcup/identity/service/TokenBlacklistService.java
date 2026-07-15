package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.RevokedTokenDocument;
import co.edu.escuelaing.techcup.identity.repository.RevokedTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;

/**
 * Tracks JWTs that have been explicitly revoked (logout), since JWTs are
 * otherwise stateless and cannot be invalidated before their natural expiry.
 * Only a hash of each token is persisted, never the raw value.
 */
@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    public TokenBlacklistService(RevokedTokenRepository revokedTokenRepository, JwtService jwtService) {
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
    }

    /**
     * Revokes a token so it can no longer be used, even though it hasn't
     * expired yet. Already-expired tokens are ignored — they're unusable
     * regardless of revocation.
     */
    public void revoke(String token) {
        Date expiration;
        try {
            expiration = jwtService.extractExpiration(token);
        } catch (ExpiredJwtException ex) {
            return;
        }

        String tokenHash = hash(token);
        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        RevokedTokenDocument revoked = RevokedTokenDocument.builder()
                .tokenHash(tokenHash)
                .expiresAt(toLocalDateTime(expiration))
                .build();

        revokedTokenRepository.save(revoked);
    }

    /**
     * Checks whether a token has been revoked.
     */
    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsByTokenHash(hash(token));
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
