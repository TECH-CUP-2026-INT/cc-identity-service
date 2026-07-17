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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Audit log query endpoints for security events. " +
        "Covers the Security Audit Query functional requirement. Only accessible to ADMIN users. " +
        "Allows filtering events by date range, action type, and specific user.")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryUseCase auditQueryUseCase;
    private final AuditEventMapper auditEventMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Query audit events with optional filters",
            description = "Returns the list of security audit events registered in the platform. " +
                    "All filters are optional; without filters, returns all events. " +
                    "Available action types include: USER_LOGIN, GOOGLE_LOGIN, USER_LOGOUT, USER_REGISTERED, " +
                    "OTP_VALIDATED, PASSWORD_RECOVERY_REQUEST, PASSWORD_RESET, TOKEN_VALIDATED, among others. " +
                    "Requires JWT authentication with ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of audit events (may be empty if no matches)."),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter (incorrect date format, non-existent action type, or startDate after endDate)."),
            @ApiResponse(responseCode = "401", description = "Missing, invalid, or expired JWT token."),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role.")
    })
    public ResponseEntity<List<AuditEventResponse>> queryEvents(
            @Parameter(description = "Start datetime of the range (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End datetime of the range (ISO format: yyyy-MM-ddTHH:mm:ss)", example = "2026-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Audit action type to filter by (e.g. USER_LOGIN, USER_LOGOUT, PASSWORD_RESET)")
            @RequestParam(required = false) AuditActionType actionType,
            @Parameter(description = "User ID to filter events for a specific user")
            @RequestParam(required = false) UUID userId) {

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
