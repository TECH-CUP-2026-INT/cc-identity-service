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
@Document(collection = "otp_tokens")
public class OtpTokenDocument {

    @Id
    private String id;

    @Indexed
    private UUID userId;

    private String code;
    private int failedAttempts;
    private boolean used;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
