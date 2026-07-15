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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recovery_tokens")
public class RecoveryTokenDocument {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String code;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
