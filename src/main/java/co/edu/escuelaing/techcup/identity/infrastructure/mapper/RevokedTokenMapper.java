package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RevokedTokenDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RevokedTokenMapper {

    RevokedTokenDocument toDocument(RevokedToken token);

    RevokedToken toDomain(RevokedTokenDocument document);
}
