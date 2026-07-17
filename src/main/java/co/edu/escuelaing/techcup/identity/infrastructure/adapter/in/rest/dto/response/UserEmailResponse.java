package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Email associated with a userId. Used by am-notification-service to resolve the actual notification recipient.")
public class UserEmailResponse {

    @Schema(description = "User email", example = "juan.perez@escuelaing.edu.co")
    private String email;
}
