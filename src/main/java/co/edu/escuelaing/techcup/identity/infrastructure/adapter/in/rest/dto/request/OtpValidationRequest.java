package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Solicitud de verificación del código OTP para completar el flujo de autenticación de dos factores")
public class OtpValidationRequest {

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID del usuario que recibió el OTP durante el login", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String userId;

    @NotBlank(message = "OTP code is required")
    @Schema(description = "Código OTP de 6 dígitos enviado al correo del usuario", example = "482917")
    private String otpCode;
}
