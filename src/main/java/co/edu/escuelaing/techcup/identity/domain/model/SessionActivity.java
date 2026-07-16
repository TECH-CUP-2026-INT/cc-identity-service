package co.edu.escuelaing.techcup.identity.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionActivity {

    private String id;
    private String token;
    private UUID userId;
    private LocalDateTime lastActivityAt;

    public boolean isExpiredByInactivity(int inactivityTimeoutMinutes) {
        return LocalDateTime.now(ZoneOffset.UTC).isAfter(this.lastActivityAt.plusMinutes(inactivityTimeoutMinutes));
    }

    public void touch() {
        this.lastActivityAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
