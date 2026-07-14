package co.edu.escuelaing.techcup.identity.repository;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
import co.edu.escuelaing.techcup.identity.entity.OtpPurpose;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends MongoRepository<OtpCodeEntity, UUID> {

    Optional<OtpCodeEntity> findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
            String code,
            UUID userId,
            LocalDateTime now
    );

    Optional<OtpCodeEntity> findByCodeAndUserIdAndUsedFalseAndExpiresAtAfterAndPurpose(
            String code,
            UUID userId,
            LocalDateTime now,
            OtpPurpose purpose
    );

    void deleteByUserIdAndExpiresAtBefore(
            UUID userId,
            LocalDateTime now
    );
}