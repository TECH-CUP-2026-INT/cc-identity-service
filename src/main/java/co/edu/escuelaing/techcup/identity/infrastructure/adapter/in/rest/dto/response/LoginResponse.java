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
@Schema(description = "Respuesta de login exitoso. El login NO está completo; se requiere verificación OTP.")
public class LoginResponse {

    @Schema(description = "ID del usuario autenticado. Necesario para el paso de verificación OTP.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Mensaje indicando el siguiente paso del flujo de autenticación", example = "OTP sent to your email. Please validate to complete login.")
    private String message;
}
