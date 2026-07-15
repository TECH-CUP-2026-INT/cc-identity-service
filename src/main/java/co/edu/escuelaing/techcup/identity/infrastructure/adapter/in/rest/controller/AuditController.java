package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.DomainException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuditQueryUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.AuditEventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Endpoints de consulta del registro de auditoría de eventos de seguridad. " +
        "Cubre el requisito funcional TC-12. Solo accesible para usuarios con rol ADMIN. " +
        "Permite filtrar eventos por rango de fechas, tipo de acción y usuario específico.")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryUseCase auditQueryUseCase;
    private final AuditEventMapper auditEventMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "TC-12: Consultar eventos de auditoría con filtros opcionales",
            description = "Retorna la lista de eventos de auditoría de seguridad registrados en la plataforma. " +
                    "Todos los filtros son opcionales; sin filtros retorna todos los eventos. " +
                    "Los tipos de acción disponibles incluyen: USER_LOGIN, GOOGLE_LOGIN, USER_LOGOUT, USER_REGISTERED, " +
                    "OTP_VALIDATED, PASSWORD_RECOVERY_REQUEST, PASSWORD_RESET, TOKEN_VALIDATED, entre otros. " +
                    "Requiere autenticación JWT con rol ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de eventos de auditoría (puede estar vacía si no hay coincidencias)."),
            @ApiResponse(responseCode = "400", description = "Parámetro de consulta inválido (formato de fecha incorrecto, tipo de acción inexistente, o startDate posterior a endDate)."),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente, inválido o expirado."),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.")
    })
    public ResponseEntity<List<AuditEventResponse>> queryEvents(
            @Parameter(description = "Fecha/hora de inicio del rango (formato ISO: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Fecha/hora de fin del rango (formato ISO: yyyy-MM-ddTHH:mm:ss)", example = "2026-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Tipo de acción de auditoría para filtrar (ej: USER_LOGIN, USER_LOGOUT, PASSWORD_RESET)")
            @RequestParam(required = false) AuditActionType actionType,
            @Parameter(description = "ID del usuario para filtrar eventos específicos de un usuario")
            @RequestParam(required = false) String userId) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new DomainException("INVALID_DATE_RANGE", "startDate must be before or equal to endDate");
        }

        List<AuditEvent> events = auditQueryUseCase.queryEvents(startDate, endDate, actionType, userId);
        List<AuditEventResponse> responses = events.stream()
                .map(auditEventMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
