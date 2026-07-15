package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditEventRepositoryPort {

    AuditEvent save(AuditEvent event);

    List<AuditEvent> findByFilters(LocalDateTime startDate, LocalDateTime endDate,
                                    AuditActionType actionType, String userId);
}
