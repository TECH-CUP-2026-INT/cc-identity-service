package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionActivityRepositoryPort {

    SessionActivity save(SessionActivity sessionActivity);

    Optional<SessionActivity> findByToken(String token);

    void deleteByToken(String token);

    List<SessionActivity> findAllByUserId(UUID userId);
}
