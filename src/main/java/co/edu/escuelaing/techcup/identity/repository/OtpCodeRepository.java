package co.edu.escuelaing.techcup.identity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import co.edu.escuelaing.techcup.identity.document.OtpCodeDocument;
import co.edu.escuelaing.techcup.identity.document.OtpPurpose;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends MongoRepository<OtpCodeDocument, UUID> {

    Optional<OtpCodeDocument> findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
            String code,
            UUID userId,
            LocalDateTime now
    );

    Optional<OtpCodeDocument> findByCodeAndUserIdAndUsedFalseAndExpiresAtAfterAndPurpose(
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
