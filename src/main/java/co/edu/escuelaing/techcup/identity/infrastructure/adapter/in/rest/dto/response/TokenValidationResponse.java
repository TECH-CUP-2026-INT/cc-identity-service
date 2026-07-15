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
@Schema(description = "Respuesta de validación de token JWT. Usado por otros microservicios para verificar autenticación.")
public class TokenValidationResponse {

    @Schema(description = "Indica si el token es válido (firma correcta, no expirado, no revocado)", example = "true")
    private boolean valid;

    @Schema(description = "ID del usuario propietario del token", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String userId;

    @Schema(description = "Correo institucional del usuario", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @Schema(description = "Rol del usuario en la plataforma (ADMIN, ORGANIZER, PLAYER, VIEWER)", example = "PLAYER")
    private String role;
}
