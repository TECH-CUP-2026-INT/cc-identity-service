package co.edu.escuelaing.techcup.identity.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import co.edu.escuelaing.techcup.identity.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.document.AuditEventType;
import co.edu.escuelaing.techcup.identity.document.AuditResult;

public class AuditEventResponse {

    private UUID id;
    private LocalDateTime timestamp;
    private UUID userId;
    private String actorEmail;
    private AuditEventType eventType;
    private AuditResult result;
    private String description;
    private String detail;

    public AuditEventResponse() {}

    public static AuditEventResponse from(AuditEventDocument entity) {
        AuditEventResponse dto = new AuditEventResponse();
        dto.id          = entity.getId();
        dto.timestamp   = entity.getTimestamp();
        dto.userId      = entity.getUserId();
        dto.actorEmail  = entity.getActorEmail();
        dto.eventType   = entity.getEventType();
        dto.result      = entity.getResult();
        dto.description = entity.getDescription();
        dto.detail      = entity.getDetail();
        return dto;
    }

    public UUID getId()              { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public UUID getUserId()          { return userId; }
    public String getActorEmail()    { return actorEmail; }
    public AuditEventType getEventType() { return eventType; }
    public AuditResult getResult()   { return result; }
    public String getDescription()   { return description; }
    public String getDetail()        { return detail; }
}
