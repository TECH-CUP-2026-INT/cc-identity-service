package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.SessionActivityDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.SessionActivityMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.SessionActivityMapper;
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
class SessionActivityRepositoryAdapterTest {

    @Mock
    private SessionActivityMongoRepository mongoRepository;
    @Mock
    private SessionActivityMapper mapper;

    @InjectMocks
    private SessionActivityRepositoryAdapter adapter;

    @Test
    void saveMapsDomainToDocumentAndBack() {
        SessionActivity activity = TestFixtures.sessionActivity();
        SessionActivityDocument document = TestFixtures.sessionActivityDocument();
        when(mapper.toDocument(activity)).thenReturn(document);
        when(mongoRepository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(activity);

        SessionActivity result = adapter.save(activity);

        assertThat(result).isSameAs(activity);
    }

    @Test
    void findByTokenMapsDocumentWhenPresent() {
        SessionActivityDocument document = TestFixtures.sessionActivityDocument();
        SessionActivity activity = TestFixtures.sessionActivity();
        when(mongoRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(activity);

        Optional<SessionActivity> result = adapter.findByToken(TestFixtures.JWT);

        assertThat(result).contains(activity);
    }

    @Test
    void findByTokenReturnsEmptyWhenNotFound() {
        when(mongoRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.empty());

        Optional<SessionActivity> result = adapter.findByToken(TestFixtures.JWT);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByTokenDelegatesToMongoRepository() {
        adapter.deleteByToken(TestFixtures.JWT);

        verify(mongoRepository).deleteByToken(TestFixtures.JWT);
    }
}
