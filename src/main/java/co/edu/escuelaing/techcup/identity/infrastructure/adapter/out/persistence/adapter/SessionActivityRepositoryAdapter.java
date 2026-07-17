package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.SessionActivityMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.SessionActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public List<SessionActivity> findAllByUserId(UUID userId) {
        return mongoRepository.findAllByUserId(userId).stream().map(mapper::toDomain).toList();
    }
}
