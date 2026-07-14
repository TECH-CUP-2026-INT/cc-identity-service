package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.entity.AuditEventEntity;
import co.edu.escuelaing.techcup.identity.entity.AuditEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditEventRepository
        extends MongoRepository<AuditEventEntity, UUID> {

    Page<AuditEventEntity> findByTimestampBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<AuditEventEntity> findByEventType(
            AuditEventType eventType,
            Pageable pageable
    );

    Page<AuditEventEntity> findByUserId(
            UUID userId,
            Pageable pageable
    );
}