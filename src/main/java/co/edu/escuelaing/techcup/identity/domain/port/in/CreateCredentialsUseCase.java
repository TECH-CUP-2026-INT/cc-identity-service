package co.edu.escuelaing.techcup.identity.domain.port.in;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.model.User;

/**
 * Internal port for inter-service credential creation.
 * Called by users-players-service when registering students, guests, graduates (TC-01, TC-02, TC-03).
 */
public interface CreateCredentialsUseCase {

    User createCredentials(String email, String password, String fullName,
                           UserType userType, UserRole role);
}
