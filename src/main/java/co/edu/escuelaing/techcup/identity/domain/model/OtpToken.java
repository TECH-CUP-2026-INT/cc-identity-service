package co.edu.escuelaing.techcup.identity.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    private String id;
    private String userId;
    private String code;
    private int failedAttempts;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    // Business methods

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isValid(String inputCode) {
        return !this.used && !isExpired() && this.code.equals(inputCode);
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    public void markAsUsed() {
        this.used = true;
    }
}
