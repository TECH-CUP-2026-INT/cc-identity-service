package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Evento de auditoría de seguridad registrado por el Identity Service")
public class AuditEventResponse {

    @Schema(description = "ID único del evento de auditoría", example = "668a1b2c3d4e5f6a7b8c9d0e")
    private String id;

    @Schema(description = "ID del usuario que generó el evento", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Tipo de acción registrada (USER_LOGIN, USER_LOGOUT, PASSWORD_RESET, etc.)", example = "USER_LOGIN")
    private AuditActionType actionType;

    @Schema(description = "Descripción legible del evento", example = "User logged in successfully via institutional email")
    private String description;

    @Schema(description = "Indica si la acción fue exitosa o fallida", example = "true")
    private boolean success;

    @Schema(description = "Fecha y hora en que ocurrió el evento", example = "2026-07-15T10:30:00")
    private LocalDateTime timestamp;
}
