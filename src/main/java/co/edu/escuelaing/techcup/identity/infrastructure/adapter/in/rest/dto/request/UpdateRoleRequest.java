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
@Schema(description = "Internal DTO to update a user's authentication role. " +
        "Consumed during captain promotion (players-service) and captaincy transfer (teams-service).")
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New authentication role for the user", example = "CAPTAIN",
            allowableValues = {"PLAYER", "CAPTAIN", "REFEREE", "ORGANIZER", "ADMIN"})
    private UserRole role;
}
