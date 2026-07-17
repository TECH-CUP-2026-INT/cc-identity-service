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
@Schema(description = "Internal DTO for account status update from users-players-service. " +
        "Usado cuando el Admin deshabilita un usuario (Deshabilitación de Usuario).")
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New account status", example = "INACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "BLOCKED"})
    private AccountStatus status;
}
