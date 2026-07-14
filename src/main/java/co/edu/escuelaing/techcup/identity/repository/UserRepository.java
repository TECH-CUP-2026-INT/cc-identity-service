package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.entity.UserEntity.UserType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdNumber(String idNumber);

    Optional<UserEntity> findByIdNumber(String idNumber);

    /** TC-02: Validates that the associatedStudentId references an existing STUDENT. */
    Optional<UserEntity> findByIdAndUserType(String id, UserType userType);
}
