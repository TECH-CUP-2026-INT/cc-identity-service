package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
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
@Document(collection = "audit_events")
public class AuditEventDocument {

    @Id
    private String id;

    @Indexed
    private UUID userId;

    private AuditActionType actionType;
    private String description;
    private boolean success;

    @Indexed
    private LocalDateTime timestamp;
}
