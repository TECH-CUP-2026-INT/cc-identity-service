package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository;

import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.AuditEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventMongoRepository extends MongoRepository<AuditEventDocument, String> {
}
