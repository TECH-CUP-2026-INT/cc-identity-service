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
@Schema(description = "Internal DTO for role update from users-players-service or teams-service. " +
        "Usado cuando un jugador se vuelve capitán (Promoción a Capitán) o se transfiere la capitanía (Transferencia de Capitanía).")
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New user role", example = "CAPTAIN",
            allowableValues = {"PLAYER", "CAPTAIN", "REFEREE", "ORGANIZER", "ADMIN"})
    private UserRole role;
}
