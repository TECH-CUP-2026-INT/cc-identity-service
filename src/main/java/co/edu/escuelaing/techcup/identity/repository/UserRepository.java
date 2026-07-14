package co.edu.escuelaing.techcup.identity.repository;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
/**
 * Repository INTERFACE for userEntity persistence operations.
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
    /**
     * Optional<userEntity> is a container, can have value or not, BUT avoid null pointer exceptions, and forces the caller to handle the case where the user is not found.
     * @param email
     * @return
     */
    Optional<UserEntity> findByEmail(String email);
    /**
     * Checks whether a user with the given email already exists.
     * @param email the email to check
     * @return true if a user with that email exists
     */
    boolean existsByEmail(String email);

    /**
     * SCRUM-22: Verifica si ya existe un usuario con ese numero de identificacion.
     * @param idNumber el numero de documento a verificar
     * @return true si ya existe un usuario con ese idNumber
     */
    boolean existsByIdNumber(String idNumber);
}