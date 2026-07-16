package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    private String id;
    private UUID userId;
    private AuditActionType actionType;
    private String description;
    private boolean success;
    private LocalDateTime timestamp;
}
