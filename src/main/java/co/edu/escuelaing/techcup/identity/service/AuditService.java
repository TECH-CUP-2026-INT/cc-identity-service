package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.document.AuditEventType;
import co.edu.escuelaing.techcup.identity.document.AuditResult;
import co.edu.escuelaing.techcup.identity.dto.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Records auditable events and exposes them for ADMIN queries.
 * A failure while recording an event must not interrupt the main operation.
 */
@Service
public class AuditService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;
    private final MongoTemplate mongoTemplate;

    public AuditService(
            AuditEventRepository auditEventRepository,
            MongoTemplate mongoTemplate
    ) {
        this.auditEventRepository = auditEventRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Persists an audit event without affecting the main operation
     * when audit persistence fails.
     */
    public void record(
            AuditEventType eventType,
            AuditResult result,
            UUID userId,
            String actorEmail,
            String description,
            String detail
    ) {
        try {
            AuditEventDocument event = AuditEventDocument.builder()
                    .eventType(eventType)
                    .result(result)
                    .userId(userId)
                    .actorEmail(actorEmail)
                    .description(description)
                    .detail(detail)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditEventRepository.save(event);
        } catch (Exception ex) {
            log.error(
                    "Failed to persist audit event [{}]: {}",
                    eventType,
                    ex.getMessage()
            );
        }
    }

    /**
     * Returns a paginated and filtered list of audit events.
     */
    public Page<AuditEventResponse> findEvents(
            LocalDateTime startDate,
            LocalDateTime endDate,
            AuditEventType eventType,
            UUID userId,
            Pageable pageable
    ) {
        validateDateRange(startDate, endDate);

        Query query = buildFilterQuery(
                startDate,
                endDate,
                eventType,
                userId
        );

        long total = mongoTemplate.count(
                query,
                AuditEventDocument.class
        );

        query.with(pageable);

        List<AuditEventDocument> events = mongoTemplate.find(
                query,
                AuditEventDocument.class
        );

        List<AuditEventResponse> responses = events.stream()
                .map(AuditEventResponse::from)
                .toList();

        return new PageImpl<>(responses, pageable, total);
    }

    private void validateDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "startDate must not be after endDate"
            );
        }
    }

    private Query buildFilterQuery(
            LocalDateTime startDate,
            LocalDateTime endDate,
            AuditEventType eventType,
            UUID userId
    ) {
        List<Criteria> filters = new ArrayList<>();

        addDateCriteria(filters, startDate, endDate);

        if (eventType != null) {
            filters.add(Criteria.where("eventType").is(eventType));
        }

        if (userId != null) {
            filters.add(Criteria.where("userId").is(userId));
        }

        Query query = new Query();

        if (!filters.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(
                            filters.toArray(new Criteria[0])
                    )
            );
        }

        return query;
    }

    private void addDateCriteria(
            List<Criteria> filters,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        if (startDate != null && endDate != null) {
            filters.add(
                    Criteria.where("timestamp")
                            .gte(startDate)
                            .lte(endDate)
            );
        } else if (startDate != null) {
            filters.add(
                    Criteria.where("timestamp").gte(startDate)
            );
        } else if (endDate != null) {
            filters.add(
                    Criteria.where("timestamp").lte(endDate)
            );
        }
    }
}