package co.edu.escuelaing.techcup.identity.domain.port.in;

import co.edu.escuelaing.techcup.identity.domain.model.User;

/**
 * TC-05: Create admin/organizer account (self-registration).
 */
public interface RegisterUserUseCase {

    User createAdminOrOrganizer(User user);
}
