package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Login request with institutional email and password")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "User institutional email (@escuelaing.edu.co)", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "MiPassword123!")
    private String password;
}
