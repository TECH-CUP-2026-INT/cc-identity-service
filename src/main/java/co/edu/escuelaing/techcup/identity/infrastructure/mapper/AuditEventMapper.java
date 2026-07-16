package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.AuditEventResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditEventMapper {

    AuditEventResponse toResponse(AuditEvent event);
}
