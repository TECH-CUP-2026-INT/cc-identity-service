package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.document.RevokedTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RevokedTokenRepository extends MongoRepository<RevokedTokenDocument, UUID> {

    boolean existsByTokenHash(String tokenHash);
}
