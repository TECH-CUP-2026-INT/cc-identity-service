package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RecoveryTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.RecoveryTokenMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.RecoveryTokenMapper;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveryTokenRepositoryAdapterTest {

    @Mock
    private RecoveryTokenMongoRepository mongoRepository;
    @Mock
    private RecoveryTokenMapper mapper;

    @InjectMocks
    private RecoveryTokenRepositoryAdapter adapter;

    @Test
    void saveMapsDomainToDocumentAndBack() {
        RecoveryToken token = TestFixtures.validRecoveryToken();
        RecoveryTokenDocument document = TestFixtures.recoveryTokenDocument();
        when(mapper.toDocument(token)).thenReturn(document);
        when(mongoRepository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(token);

        RecoveryToken result = adapter.save(token);

        assertThat(result).isSameAs(token);
    }

    @Test
    void findLatestByUserIdMapsDocumentWhenPresent() {
        RecoveryToken token = TestFixtures.validRecoveryToken();
        RecoveryTokenDocument document = TestFixtures.recoveryTokenDocument();
        when(mongoRepository.findTopByUserIdOrderByCreatedAtDesc(TestFixtures.USER_ID)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(token);

        Optional<RecoveryToken> result = adapter.findLatestByUserId(TestFixtures.USER_ID);

        assertThat(result).contains(token);
    }

    @Test
    void findLatestByUserIdReturnsEmptyWhenMissing() {
        when(mongoRepository.findTopByUserIdOrderByCreatedAtDesc("missing")).thenReturn(Optional.empty());

        Optional<RecoveryToken> result = adapter.findLatestByUserId("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAllByUserIdDelegatesToMongoRepository() {
        adapter.deleteAllByUserId(TestFixtures.USER_ID);

        verify(mongoRepository).deleteAllByUserId(TestFixtures.USER_ID);
    }
}
