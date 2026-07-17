package co.edu.escuelaing.techcup.identity.domain.port.in;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;

import java.util.UUID;

/**
 * Internal port for inter-service credential updates.
 * Called by users-players-service (role change on captain promotion, status change on user disabling)
 * and by teams-service (captaincy transfer).
 */
public interface UpdateCredentialsUseCase {

    void updateRole(UUID userId, UserRole role);

    void updateStatus(UUID userId, AccountStatus status);
}
