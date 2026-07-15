package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository;

import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RevokedTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface RevokedTokenMongoRepository extends MongoRepository<RevokedTokenDocument, String> {

    boolean existsByToken(String token);

    void deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}
