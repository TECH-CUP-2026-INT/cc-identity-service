package co.edu.escuelaing.techcup.identity.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a JWT that has been explicitly revoked (logout).
 * Only a SHA-256 hash of the token is stored, never the raw value.
 * Documents auto-expire via a TTL index once the original token's own
 * expiration passes, since it would be unusable after that point anyway.
 */
@Document(collection = "revoked_tokens")
public class RevokedTokenDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String tokenHash;

    @Indexed(expireAfter = "0s")
    private LocalDateTime expiresAt;

    public RevokedTokenDocument() {
    }

    private RevokedTokenDocument(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.tokenHash = builder.tokenHash;
        this.expiresAt = builder.expiresAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;
        private String tokenHash;
        private LocalDateTime expiresAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder tokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public RevokedTokenDocument build() {
            return new RevokedTokenDocument(this);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
