package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.AuditEventMapper;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.AuditEventMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditEventRepositoryAdapter implements AuditEventRepositoryPort {

    private static final String TIMESTAMP_FIELD = "timestamp";

    private final AuditEventMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;
    private final AuditEventMapper mapper;

    @Override
    public AuditEvent save(AuditEvent event) {
        var document = mapper.toDocument(event);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public List<AuditEvent> findByFilters(LocalDateTime startDate, LocalDateTime endDate,
                                           AuditActionType actionType, String userId) {
        Query query = new Query();

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where(TIMESTAMP_FIELD).gte(startDate).lte(endDate));
        } else if (startDate != null) {
            query.addCriteria(Criteria.where(TIMESTAMP_FIELD).gte(startDate));
        } else if (endDate != null) {
            query.addCriteria(Criteria.where(TIMESTAMP_FIELD).lte(endDate));
        }

        if (actionType != null) {
            query.addCriteria(Criteria.where("actionType").is(actionType));
        }

        if (userId != null && !userId.isBlank()) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }

        List<AuditEventDocument> documents = mongoTemplate.find(query, AuditEventDocument.class);
        return documents.stream().map(mapper::toDomain).toList();
    }
}
