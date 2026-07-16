package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO interno para actualización de rol desde users-players-service o teams-service. " +
        "Usado cuando un jugador se vuelve capitán (TC-18) o se transfiere la capitanía (TC-27).")
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "Nuevo rol del usuario", example = "CAPTAIN",
            allowableValues = {"PLAYER", "CAPTAIN", "REFEREE", "ORGANIZER", "ADMIN"})
    private UserRole role;
}
