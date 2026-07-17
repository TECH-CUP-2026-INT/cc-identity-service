package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Successful OTP verification response. Contains JWT and authenticated user data.")
public class OtpResponse {

    @Schema(description = "JWT token for authentication in subsequent requests. Include as 'Bearer <token>' en el header Authorization.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Authenticated user data")
    private UserResponse user;
}
