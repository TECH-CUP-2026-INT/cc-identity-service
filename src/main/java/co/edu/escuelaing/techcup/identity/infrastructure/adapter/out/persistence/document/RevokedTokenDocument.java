package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "revoked_tokens")
public class RevokedTokenDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @Indexed
    private UUID userId;

    private LocalDateTime revokedAt;

    @Indexed(expireAfter = "0s")
    private LocalDateTime expiresAt;
}
