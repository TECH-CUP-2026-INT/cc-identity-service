package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.entity.UserEntity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserEntity persistence operations.
 * Extends JpaRepository to inherit standard CRUD methods.
 * Custom queries are derived from method names by Spring Data JPA.
 *
 * @see UserEntity
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds a user by their email address.
     * @param email the email to search for
     * @return an Optional containing the user if found, empty otherwise
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     * @param email the email to check
     * @return true if a user with that email exists
     */
    boolean existsByEmail(String email);

    Optional<UserEntity> findByIdNumber(String idNumber);

    boolean existsByIdNumber(String idNumber);

    /**
     * Finds a user by their UUID and userType.
     * Used in TC-02 (Guest registration) to validate that the
     * associatedStudentId references an existing STUDENT user.
     *
     * @param id       the UUID of the user to find
     * @param userType the expected user type (STUDENT)
     * @return an Optional containing the user if found with that type
     */
    Optional<UserEntity> findByIdAndUserType(UUID id, UserType userType);
}