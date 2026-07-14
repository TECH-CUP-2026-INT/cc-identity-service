package co.edu.escuelaing.techcup.identity.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an OTP code stored in MongoDB.
 */
@Document(collection = "otp_codes")
@CompoundIndex(
        name = "otp_validation_idx",
        def = "{'code': 1, 'userId': 1, 'purpose': 1, 'used': 1, 'expiresAt': 1}"
)
public class OtpCodeDocument {

    @Id
    private UUID id;

    private String code;

    @Indexed
    private UUID userId;

    private OtpPurpose purpose;

    @Indexed
    private LocalDateTime expiresAt;

    private boolean used = false;

    @CreatedDate
    private LocalDateTime createdAt;

    public OtpCodeDocument() {
    }

    private OtpCodeDocument(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.code = builder.code;
        this.userId = builder.userId;
        this.purpose = builder.purpose;
        this.expiresAt = builder.expiresAt;
        this.used = builder.used;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;
        private String code;
        private UUID userId;
        private OtpPurpose purpose;
        private LocalDateTime expiresAt;
        private boolean used = false;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder purpose(OtpPurpose purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder used(boolean used) {
            this.used = used;
            return this;
        }

        public OtpCodeDocument build() {
            return new OtpCodeDocument(this);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public UUID getUserId() {
        return userId;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}