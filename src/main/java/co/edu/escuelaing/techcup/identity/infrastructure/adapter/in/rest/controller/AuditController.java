package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuditQueryUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.AuditEventMapper;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Audit", description = "Audit event query endpoints (TC-12)")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryUseCase auditQueryUseCase;
    private final AuditEventMapper auditEventMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "TC-12: Query audit events with optional filters")
    public ResponseEntity<List<AuditEventResponse>> queryEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(required = false) String userId) {

        List<AuditEvent> events = auditQueryUseCase.queryEvents(startDate, endDate, actionType, userId);
        List<AuditEventResponse> responses = events.stream()
                .map(auditEventMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
