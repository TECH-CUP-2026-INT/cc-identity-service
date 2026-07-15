package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.OtpTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.OtpTokenMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.OtpTokenMapper;
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
class OtpRepositoryAdapterTest {

    @Mock
    private OtpTokenMongoRepository mongoRepository;
    @Mock
    private OtpTokenMapper mapper;

    @InjectMocks
    private OtpRepositoryAdapter adapter;

    @Test
    void saveMapsDomainToDocumentAndBack() {
        OtpToken token = TestFixtures.validOtp();
        OtpTokenDocument document = TestFixtures.otpTokenDocument();
        when(mapper.toDocument(token)).thenReturn(document);
        when(mongoRepository.save(document)).thenReturn(document);
        when(mapper.toDomain(document)).thenReturn(token);

        OtpToken result = adapter.save(token);

        assertThat(result).isSameAs(token);
        verify(mongoRepository).save(document);
    }

    @Test
    void findLatestByUserIdMapsDocumentWhenPresent() {
        OtpToken token = TestFixtures.validOtp();
        OtpTokenDocument document = TestFixtures.otpTokenDocument();
        when(mongoRepository.findTopByUserIdOrderByCreatedAtDesc(TestFixtures.USER_ID)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(token);

        Optional<OtpToken> result = adapter.findLatestByUserId(TestFixtures.USER_ID);

        assertThat(result).contains(token);
    }

    @Test
    void findLatestByUserIdReturnsEmptyWhenMissing() {
        when(mongoRepository.findTopByUserIdOrderByCreatedAtDesc("missing")).thenReturn(Optional.empty());

        Optional<OtpToken> result = adapter.findLatestByUserId("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAllByUserIdDelegatesToMongoRepository() {
        adapter.deleteAllByUserId(TestFixtures.USER_ID);

        verify(mongoRepository).deleteAllByUserId(TestFixtures.USER_ID);
    }
}
