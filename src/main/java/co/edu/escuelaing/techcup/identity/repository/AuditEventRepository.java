package co.edu.escuelaing.techcup.identity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import co.edu.escuelaing.techcup.identity.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.document.AuditEventType;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditEventRepository
        extends MongoRepository<AuditEventDocument, UUID> {

    Page<AuditEventDocument> findByTimestampBetween(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<AuditEventDocument> findByEventType(
            AuditEventType eventType,
            Pageable pageable
    );

    Page<AuditEventDocument> findByUserId(
            UUID userId,
            Pageable pageable
    );
}