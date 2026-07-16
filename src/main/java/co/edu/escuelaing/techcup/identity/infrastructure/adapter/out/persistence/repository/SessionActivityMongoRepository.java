package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository;

import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.SessionActivityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SessionActivityMongoRepository extends MongoRepository<SessionActivityDocument, String> {

    Optional<SessionActivityDocument> findByToken(String token);

    void deleteByToken(String token);
}
