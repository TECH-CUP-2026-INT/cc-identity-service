package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Correo electrónico asociado a un userId. Usado por am-notification-service para resolver el destinatario real de una notificación.")
public class UserEmailResponse {

    @Schema(description = "Correo del usuario", example = "juan.perez@escuelaing.edu.co")
    private String email;
}
