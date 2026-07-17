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
@Schema(description = "Password reset request using the recovery code received by email")
public class PasswordResetRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Institutional email associated with the account", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Recovery code is required")
    @Schema(description = "Single-use recovery code received by email (expires in 15 minutes by default)", example = "a1b2c3d4e5f6")
    private String recoveryCode;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password for the account", example = "NuevaPassword456!")
    private String newPassword;
}
