package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.OtpTokenMapper;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.OtpTokenMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpRepositoryAdapter implements OtpRepositoryPort {

    private final OtpTokenMongoRepository mongoRepository;
    private final OtpTokenMapper mapper;

    @Override
    public OtpToken save(OtpToken otpToken) {
        var document = mapper.toDocument(otpToken);
        var saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OtpToken> findLatestByUserId(String userId) {
        return mongoRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteAllByUserId(String userId) {
        mongoRepository.deleteAllByUserId(userId);
    }
}
