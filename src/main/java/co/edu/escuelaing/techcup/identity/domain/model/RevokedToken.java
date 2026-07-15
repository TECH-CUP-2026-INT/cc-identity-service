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
public class RevokedToken {

    private String id;
    private String token;
    private String userId;
    private LocalDateTime revokedAt;
    private LocalDateTime expiresAt;
}
