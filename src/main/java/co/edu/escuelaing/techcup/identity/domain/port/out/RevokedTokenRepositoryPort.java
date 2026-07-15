package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;

public interface RevokedTokenRepositoryPort {

    RevokedToken save(RevokedToken revokedToken);

    boolean existsByToken(String token);

    void deleteExpiredTokens();
}
