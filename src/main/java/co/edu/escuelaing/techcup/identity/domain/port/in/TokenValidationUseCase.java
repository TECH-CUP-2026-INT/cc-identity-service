package co.edu.escuelaing.techcup.identity.domain.port.in;

import co.edu.escuelaing.techcup.identity.domain.model.User;

public interface TokenValidationUseCase {

    /**
     * TC-08: Validate JWT token. Returns user data if valid.
     */
    User validateToken(String token);
}
