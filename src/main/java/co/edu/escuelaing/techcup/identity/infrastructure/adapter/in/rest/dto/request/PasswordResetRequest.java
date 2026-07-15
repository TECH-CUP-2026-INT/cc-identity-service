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
@Schema(description = "Solicitud de restablecimiento de contraseña usando el código de recuperación recibido por correo")
public class PasswordResetRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Correo institucional asociado a la cuenta", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Recovery code is required")
    @Schema(description = "Código de recuperación de un solo uso recibido por correo (expira en 15 minutos por defecto)", example = "a1b2c3d4e5f6")
    private String recoveryCode;

    @NotBlank(message = "New password is required")
    @Schema(description = "Nueva contraseña para la cuenta", example = "NuevaPassword456!")
    private String newPassword;
}
