package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.SessionActivityMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.SessionActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionActivityRepositoryAdapter implements SessionActivityRepositoryPort {

    private final SessionActivityMongoRepository mongoRepository;
    private final SessionActivityMapper mapper;

    @Override
    public SessionActivity save(SessionActivity sessionActivity) {
        var document = mapper.toDocument(sessionActivity);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SessionActivity> findByToken(String token) {
        return mongoRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public void deleteByToken(String token) {
        mongoRepository.deleteByToken(token);
    }
}
