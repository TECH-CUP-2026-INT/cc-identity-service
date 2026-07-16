package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.SessionActivityDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SessionActivityMapper {

    SessionActivityDocument toDocument(SessionActivity sessionActivity);

    SessionActivity toDomain(SessionActivityDocument document);
}
