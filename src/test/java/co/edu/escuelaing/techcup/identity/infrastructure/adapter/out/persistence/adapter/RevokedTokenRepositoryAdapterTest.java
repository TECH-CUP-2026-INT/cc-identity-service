package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RevokedTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.RevokedTokenMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.RevokedTokenMapper;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokedTokenRepositoryAdapterTest {

    @Mock
    private RevokedTokenMongoRepository mongoRepository;
    @Mock
    private RevokedTokenMapper mapper;

    @InjectMocks
    private RevokedTokenRepositoryAdapter adapter;

    @Test
    void saveMapsDomainToDocumentAndBack() {
        RevokedToken token = TestFixtures.revokedToken();
        RevokedTokenDocument document = TestFixtures.revokedTokenDocument();
        when(mapper.toDocument(token)).thenReturn(document);
        when(mongoRepository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(token);

        RevokedToken result = adapter.save(token);

        assertThat(result).isSameAs(token);
    }

    @Test
    void existsByTokenDelegatesToMongoRepository() {
        when(mongoRepository.existsByToken(TestFixtures.JWT)).thenReturn(true);

        boolean exists = adapter.existsByToken(TestFixtures.JWT);

        assertThat(exists).isTrue();
        verify(mongoRepository).existsByToken(TestFixtures.JWT);
    }

    @Test
    void deleteExpiredTokensUsesCurrentDateAsCutoff() {
        adapter.deleteExpiredTokens();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mongoRepository).deleteAllByExpiresAtBefore(cutoffCaptor.capture());
        assertThat(cutoffCaptor.getValue()).isBeforeOrEqualTo(LocalDateTime.now(ZoneOffset.UTC));
    }
}
