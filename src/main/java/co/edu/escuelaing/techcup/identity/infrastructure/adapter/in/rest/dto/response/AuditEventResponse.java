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
@Schema(description = "Security audit event recorded by Identity Service")
public class AuditEventResponse {

    @Schema(description = "Unique audit event ID", example = "668a1b2c3d4e5f6a7b8c9d0e")
    private String id;

    @Schema(description = "ID of the user who triggered the event", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @Schema(description = "Recorded action type (USER_LOGIN, USER_LOGOUT, PASSWORD_RESET, etc.)", example = "USER_LOGIN")
    private AuditActionType actionType;

    @Schema(description = "Human-readable event description", example = "User logged in successfully via institutional email")
    private String description;

    @Schema(description = "Indicates whether the action was successful", example = "true")
    private boolean success;

    @Schema(description = "Event timestamp", example = "2026-07-15T10:30:00")
    private LocalDateTime timestamp;
}
