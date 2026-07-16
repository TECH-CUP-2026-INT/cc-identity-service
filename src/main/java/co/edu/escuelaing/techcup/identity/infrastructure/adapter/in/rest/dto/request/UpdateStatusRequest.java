package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
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
@Schema(description = "DTO interno para actualización de estado de cuenta desde users-players-service. " +
        "Usado cuando el Admin deshabilita un usuario (TC-19).")
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "Nuevo estado de la cuenta", example = "INACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "BLOCKED"})
    private AccountStatus status;
}
