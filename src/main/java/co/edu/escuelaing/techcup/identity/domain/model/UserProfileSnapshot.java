package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Rol y estado actuales de un usuario, tal como los reporta
 * users-players-service (fuente de verdad) en el momento de la consulta.
 * Identity ya no guarda su propia copia sincronizada de estos dos campos;
 * los consulta en vivo cuando los necesita (login y validación de OTP).
 */
@Getter
@AllArgsConstructor
public class UserProfileSnapshot {

    private final UserRole role;
    private final AccountStatus status;

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
