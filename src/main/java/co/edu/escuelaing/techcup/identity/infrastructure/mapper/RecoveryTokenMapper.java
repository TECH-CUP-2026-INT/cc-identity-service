package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RecoveryTokenDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecoveryTokenMapper {

    RecoveryTokenDocument toDocument(RecoveryToken token);

    RecoveryToken toDomain(RecoveryTokenDocument document);
}
