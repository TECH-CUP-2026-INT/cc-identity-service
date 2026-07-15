package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateAdminOrganizerRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDocument toDocument(User user);

    User toDomain(UserDocument document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "idType", ignore = true)
    @Mapping(target = "idNumber", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "academicProgram", ignore = true)
    @Mapping(target = "semester", ignore = true)
    @Mapping(target = "associatedStudentId", ignore = true)
    @Mapping(target = "relationship", ignore = true)
    @Mapping(target = "formerAcademicProgram", ignore = true)
    User toDomain(CreateAdminOrganizerRequest request);

    UserResponse toResponse(User user);
}
