package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;

import java.util.Optional;

public interface OtpRepositoryPort {

    OtpToken save(OtpToken otpToken);

    Optional<OtpToken> findLatestByUserId(String userId);

    void deleteAllByUserId(String userId);
}
