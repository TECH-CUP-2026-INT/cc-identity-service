package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository;

import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMongoRepository extends MongoRepository<UserDocument, UUID> {

    Optional<UserDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
