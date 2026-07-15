package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;

import java.util.Optional;

public interface RecoveryTokenRepositoryPort {

    RecoveryToken save(RecoveryToken token);

    Optional<RecoveryToken> findLatestByUserId(String userId);

    void deleteAllByUserId(String userId);
}
