package co.edu.escuelaing.techcup.identity.domain.port.in;

import java.util.UUID;

/**
 * Internal port consumed by users-players-service right after deshabilitar
 * un usuario (TC-19), para cortar de inmediato cualquier sesión/JWT activo
 * de esa cuenta, en vez de esperar a que expire por su cuenta.
 */
public interface RevokeUserSessionsUseCase {

    void revokeAllSessions(UUID userId);
}
