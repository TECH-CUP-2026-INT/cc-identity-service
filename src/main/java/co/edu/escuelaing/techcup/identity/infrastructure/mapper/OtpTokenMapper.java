package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.OtpTokenDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OtpTokenMapper {

    OtpTokenDocument toDocument(OtpToken otpToken);

    OtpToken toDomain(OtpTokenDocument document);
}
