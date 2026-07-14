package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de persistencia para UserEntity en MongoDB.
 * Extiende MongoRepository para heredar operaciones CRUD estandar.
 * Las consultas personalizadas se derivan del nombre del metodo por Spring Data.
 *
 * @see UserEntity
 */
@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    /**
     * Busca un usuario por su email.
     * @param email el email a buscar
     * @return Optional con el usuario si existe, vacio si no
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con ese email.
     * @param email el email a verificar
     * @return true si existe un usuario con ese email
     */
    boolean existsByEmail(String email);

    /**
     * SCRUM-22: Verifica si ya existe un usuario con ese numero de identificacion.
     * @param idNumber el numero de documento a verificar
     * @return true si ya existe un usuario con ese idNumber
     */
    boolean existsByIdNumber(String idNumber);
}
