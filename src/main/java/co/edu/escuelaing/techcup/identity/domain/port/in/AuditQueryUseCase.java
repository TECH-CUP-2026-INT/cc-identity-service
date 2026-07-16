package co.edu.escuelaing.techcup.identity.domain.port.in;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditQueryUseCase {

    /**
     * TC-12: Consult Identity Service audit events with optional filters.
     */
    List<AuditEvent> queryEvents(LocalDateTime startDate, LocalDateTime endDate,
                                  AuditActionType actionType, UUID userId);
}
