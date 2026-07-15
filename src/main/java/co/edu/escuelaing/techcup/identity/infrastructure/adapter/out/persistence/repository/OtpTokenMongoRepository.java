package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository;

import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.OtpTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpTokenMongoRepository extends MongoRepository<OtpTokenDocument, String> {

    Optional<OtpTokenDocument> findTopByUserIdOrderByCreatedAtDesc(String userId);

    void deleteAllByUserId(String userId);
}
