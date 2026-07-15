package co.edu.escuelaing.techcup.identity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.document.UserDocument.UserType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<UserDocument, UUID> {

    Optional<UserDocument> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdNumber(String idNumber);

    Optional<UserDocument> findByIdNumber(String idNumber);

    /** TC-02: Validates that the associatedStudentId references an existing STUDENT. */
    Optional<UserDocument> findByIdAndUserType(UUID id, UserType userType);
}
