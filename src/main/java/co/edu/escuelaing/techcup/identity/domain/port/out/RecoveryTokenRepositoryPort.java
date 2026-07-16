package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;

import java.util.Optional;
import java.util.UUID;

public interface RecoveryTokenRepositoryPort {

    RecoveryToken save(RecoveryToken token);

    Optional<RecoveryToken> findLatestByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
