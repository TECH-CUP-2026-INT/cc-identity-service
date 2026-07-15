package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository;

import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RecoveryTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecoveryTokenMongoRepository extends MongoRepository<RecoveryTokenDocument, String> {

    Optional<RecoveryTokenDocument> findTopByUserIdOrderByCreatedAtDesc(String userId);

    void deleteAllByUserId(String userId);
}
