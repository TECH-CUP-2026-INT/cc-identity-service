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
@Schema(description = "Successful login response. Login is NOT complete; OTP verification is required.")
public class LoginResponse {

    @Schema(description = "Authenticated user ID. Required for the OTP verification step.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Message indicating the next step in the authentication flow", example = "OTP sent to your email. Please validate to complete login.")
    private String message;
}
