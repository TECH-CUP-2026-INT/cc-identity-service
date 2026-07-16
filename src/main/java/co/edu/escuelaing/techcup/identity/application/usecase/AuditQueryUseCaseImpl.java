package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuditQueryUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuditQueryUseCaseImpl implements AuditQueryUseCase {

    private final AuditEventRepositoryPort auditRepository;

    @Override
    public List<AuditEvent> queryEvents(LocalDateTime startDate, LocalDateTime endDate,
                                         AuditActionType actionType, UUID userId) {
        log.info("Querying audit events with filters");
        return auditRepository.findByFilters(startDate, endDate, actionType, userId);
    }
}
