package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT token validation response. Used by other microservices to verify authentication.")
public class TokenValidationResponse {

    @Schema(description = "Indicates whether the token is valid (correct signature, not expired, not revoked)", example = "true")
    private boolean valid;

    @Schema(description = "ID of the token owner user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "User institutional email", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @Schema(description = "User role in the platform (ADMIN, ORGANIZER, PLAYER, VIEWER)", example = "PLAYER")
    private String role;
}
