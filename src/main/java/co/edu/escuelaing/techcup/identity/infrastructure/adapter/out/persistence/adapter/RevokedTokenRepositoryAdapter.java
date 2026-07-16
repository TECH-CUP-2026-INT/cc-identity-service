package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.RevokedTokenMapper;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.RevokedTokenMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class RevokedTokenRepositoryAdapter implements RevokedTokenRepositoryPort {

    private final RevokedTokenMongoRepository mongoRepository;
    private final RevokedTokenMapper mapper;

    @Override
    public RevokedToken save(RevokedToken revokedToken) {
        var document = mapper.toDocument(revokedToken);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByToken(String token) {
        return mongoRepository.existsByToken(token);
    }

    @Override
    public void deleteExpiredTokens() {
        mongoRepository.deleteAllByExpiresAtBefore(LocalDateTime.now(ZoneOffset.UTC));
    }
}
