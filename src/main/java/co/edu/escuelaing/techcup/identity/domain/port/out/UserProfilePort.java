package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.UserProfileSnapshot;

import java.util.UUID;

/**
 * Consulta en vivo del rol/estado actual de un usuario en
 * users-players-service (fuente de verdad de identidad). Identity ya no
 * recibe estos datos por push; los pregunta cuando los necesita.
 */
public interface UserProfilePort {

    /**
     * @throws co.edu.escuelaing.techcup.identity.domain.exception.UserProfileUnavailableException
     *         si users-players-service no está disponible o responde con error
     */
    UserProfileSnapshot fetchProfile(UUID userId);
}
