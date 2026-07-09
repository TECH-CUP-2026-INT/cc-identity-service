package co.edu.escuelaing.techcup.identity.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
/**
 * Represents a one-time password (OTP) code sent to a user via email.
 * Maps to the {@code otp_codes} table in the database.
 * Each OTP is linked to a user, has an expiration time, and can only be used once.
 * Once verified, the used flag is set to true to prevent reuse (SCRUM-13).
 *
 * @see UserEntity
 */
@Entity
@Table(name = "otp_codes")
public class otpCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 6)
    private String code;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private userEntity user;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private boolean used = false;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor (empty)
     */
    public otpCodeEntity() {}

    /**
     * Private constructor used exclusively by the Builder.
     * @param builder the builder instance containing the field values
     */
    private otpCodeEntity(Builder builder) {
        this.code = builder.code;
        this.user = builder.user;
        this.expiresAt = builder.expiresAt;
        this.used = builder.used;
    }
    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private String code;
        private userEntity user;
        private LocalDateTime expiresAt;
        private boolean used = false;

        public Builder code(String code) { this.code = code; return this; }
        public Builder user(userEntity user) { this.user = user; return this; }
        public Builder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder used(boolean used) { this.used = used; return this; }
        public otpCodeEntity build() { return new otpCodeEntity(this); }
    }
    /**
     * Getters
     * @return
     */
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public userEntity getUser() { return user; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * Setters
     * @param code
     */
    public void setCode(String code) { this.code = code; }
    public void setUser(userEntity user) { this.user = user; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setUsed(boolean used) { this.used = used; }
}