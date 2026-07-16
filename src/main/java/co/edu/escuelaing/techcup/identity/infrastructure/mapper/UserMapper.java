package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDocument toDocument(User user);

    User toDomain(UserDocument document);

    UserResponse toResponse(User user);
}
