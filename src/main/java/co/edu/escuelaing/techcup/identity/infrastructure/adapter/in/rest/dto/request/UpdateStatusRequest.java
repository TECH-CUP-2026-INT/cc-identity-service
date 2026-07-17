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
@Schema(description = "Internal DTO to update a user's account status. " +
        "Consumed when an admin disables a user (User Disabling). An INACTIVE account cannot log in.")
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New account status for the user", example = "INACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"})
    private AccountStatus status;
}
