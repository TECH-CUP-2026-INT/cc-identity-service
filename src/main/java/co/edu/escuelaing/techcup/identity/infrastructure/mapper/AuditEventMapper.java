package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.AuditEventDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditEventMapper {

    AuditEventDocument toDocument(AuditEvent event);

    AuditEvent toDomain(AuditEventDocument document);

    AuditEventResponse toResponse(AuditEvent event);
}
