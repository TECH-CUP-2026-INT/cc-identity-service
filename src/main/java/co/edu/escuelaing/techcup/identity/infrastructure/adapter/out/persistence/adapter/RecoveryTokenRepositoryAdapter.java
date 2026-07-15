package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.domain.port.out.RecoveryTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.RecoveryTokenMapper;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.RecoveryTokenMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecoveryTokenRepositoryAdapter implements RecoveryTokenRepositoryPort {

    private final RecoveryTokenMongoRepository mongoRepository;
    private final RecoveryTokenMapper mapper;

    @Override
    public RecoveryToken save(RecoveryToken token) {
        var document = mapper.toDocument(token);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RecoveryToken> findLatestByUserId(String userId) {
        return mongoRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteAllByUserId(String userId) {
        mongoRepository.deleteAllByUserId(userId);
    }
}
