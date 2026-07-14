package co.edu.escuelaing.techcup.identity.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Representa un codigo OTP de un solo uso enviado al usuario por email.
 * Mapeado a la coleccion {@code otp_codes} en MongoDB.
 * Cada OTP esta vinculado al ID del usuario, tiene fecha de expiracion y solo puede usarse una vez.
 * Una vez verificado, el campo used se marca true para evitar reuso (SCRUM-13).
 *
 * @see UserEntity
 */
@Document(collection = "otp_codes")
public class OtpCodeEntity {

    @Id
    private String id;

    private String code;

    /** ID del usuario propietario de este OTP. Referencia a UserEntity. */
    private String userId;

    private LocalDateTime expiresAt;
    private boolean used = false;

    @CreatedDate
    private LocalDateTime createdAt;

    public OtpCodeEntity() {}

    /**
     * Constructor privado usado exclusivamente por el Builder.
     * @param builder instancia del builder con los valores de los campos
     */
    private OtpCodeEntity(Builder builder) {
        this.code = builder.code;
        this.userId = builder.userId;
        this.expiresAt = builder.expiresAt;
        this.used = builder.used;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String userId;
        private LocalDateTime expiresAt;
        private boolean used = false;

        public Builder code(String code) { this.code = code; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder used(boolean used) { this.used = used; return this; }
        public OtpCodeEntity build() { return new OtpCodeEntity(this); }
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getUserId() { return userId; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCode(String code) { this.code = code; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setUsed(boolean used) { this.used = used; }
}
