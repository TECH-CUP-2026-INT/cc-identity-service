package co.edu.escuelaing.techcup.identity.domain.port.out;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;

import java.util.Optional;

public interface SessionActivityRepositoryPort {

    SessionActivity save(SessionActivity sessionActivity);

    Optional<SessionActivity> findByToken(String token);

    void deleteByToken(String token);
}
