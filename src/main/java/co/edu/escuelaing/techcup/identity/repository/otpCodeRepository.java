package co.edu.escuelaing.techcup.identity.repository;
import co.edu.escuelaing.techcup.identity.entity.otpCodeEntity;
import co.edu.escuelaing.techcup.identity.entity.userEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
/**
 * Repository interface for OtpCodeEntity persistence operations
 * Provides methods to find, validate, and clean up OTP codes
 * @see OtpCodeEntity
 */
@Repository
public interface otpCodeRepository extends JpaRepository<otpCodeEntity, UUID> {

    /**
     * Finds the most recent unused and non-expired OTP for a given user and code.
     * @param code the OTP code entered by the user
     * @param user the user who requested the OTP
     * @param now current timestamp to check expiration
     * @return an Optional containing the OTP if valid, empty otherwise
     */
    Optional<otpCodeEntity> findByCodeAndUserAndUsedFalseAndExpiresAtAfter(
            String code, userEntity user, LocalDateTime now);
    /**
     * Deletes all expired OTP codes for a given user.
     * @param user the user whose expired OTPs should be removed
     * @param now current timestamp
     */
    @Modifying
    @Query("DELETE FROM OtpCodeEntity o WHERE o.user = :user AND o.expiresAt < :now")
    void deleteExpiredByUser(@Param("user") userEntity user, @Param("now") LocalDateTime now);
}
