package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio de persistencia para OtpCodeEntity en MongoDB.
 * Provee metodos para buscar, validar y limpiar codigos OTP.
 *
 * @see OtpCodeEntity
 */
@Repository
public interface OtpCodeRepository extends MongoRepository<OtpCodeEntity, String> {

    /**
     * Busca el OTP no usado y no expirado para un usuario y codigo dados.
     * @param code el codigo OTP ingresado por el usuario
     * @param userId el ID del usuario que solicito el OTP
     * @param now timestamp actual para verificar expiracion
     * @return Optional con el OTP si es valido, vacio si no
     */
    Optional<OtpCodeEntity> findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
            String code, String userId, LocalDateTime now);

    /**
     * Elimina todos los OTP expirados de un usuario dado.
     * @param userId el ID del usuario cuyos OTP expirados deben eliminarse
     * @param now timestamp actual
     */
    void deleteByUserIdAndExpiresAtBefore(String userId, LocalDateTime now);
}
