package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepositoryPort {

    OtpToken save(OtpToken otpToken);

    Optional<OtpToken> findLatestByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
