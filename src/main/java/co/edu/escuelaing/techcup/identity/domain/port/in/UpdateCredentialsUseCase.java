package co.edu.escuelaing.techcup.identity.domain.port.in;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;

import java.util.UUID;

/**
 * Internal port for inter-service credential updates.
 * Called by users-players-service (TC-18, TC-19) and teams-service (TC-27)
 * to keep Identity's auth records in sync.
 */
public interface UpdateCredentialsUseCase {

    void updateRole(UUID userId, UserRole newRole);

    void updateStatus(UUID userId, AccountStatus newStatus);
}
