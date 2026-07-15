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
@Schema(description = "Respuesta de verificación OTP exitosa. Contiene el JWT y datos del usuario autenticado.")
public class OtpResponse {

    @Schema(description = "Token JWT para autenticación en requests posteriores. Incluir como 'Bearer <token>' en el header Authorization.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Datos del usuario autenticado")
    private UserResponse user;
}
