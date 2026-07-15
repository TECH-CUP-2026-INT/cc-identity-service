package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.adapter;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.repository.UserMongoRepository;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
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
class UserRepositoryAdapterTest {

    @Mock
    private UserMongoRepository mongoRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    @Test
    void saveMapsDomainToDocumentAndBack() {
        User user = TestFixtures.activeUser();
        UserDocument document = TestFixtures.userDocument();
        User savedUser = TestFixtures.activeUser();
        savedUser.setId("saved-user");
        UserDocument savedDocument = TestFixtures.userDocument();
        savedDocument.setId("saved-user");
        when(userMapper.toDocument(user)).thenReturn(document);
        when(mongoRepository.save(document)).thenReturn(savedDocument);
        when(userMapper.toDomain(savedDocument)).thenReturn(savedUser);

        User result = adapter.save(user);

        assertThat(result).isSameAs(savedUser);
        verify(mongoRepository).save(document);
    }

    @Test
    void findByIdMapsDocumentWhenPresent() {
        UserDocument document = TestFixtures.userDocument();
        User user = TestFixtures.activeUser();
        when(mongoRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(document));
        when(userMapper.toDomain(document)).thenReturn(user);

        Optional<User> result = adapter.findById(TestFixtures.USER_ID);

        assertThat(result).contains(user);
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        when(mongoRepository.findById("missing")).thenReturn(Optional.empty());

        Optional<User> result = adapter.findById("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailMapsDocumentWhenPresent() {
        UserDocument document = TestFixtures.userDocument();
        User user = TestFixtures.activeUser();
        when(mongoRepository.findByEmail(TestFixtures.EMAIL)).thenReturn(Optional.of(document));
        when(userMapper.toDomain(document)).thenReturn(user);

        Optional<User> result = adapter.findByEmail(TestFixtures.EMAIL);

        assertThat(result).contains(user);
    }

    @Test
    void existsByEmailDelegatesToMongoRepository() {
        when(mongoRepository.existsByEmail(TestFixtures.EMAIL)).thenReturn(true);

        boolean exists = adapter.existsByEmail(TestFixtures.EMAIL);

        assertThat(exists).isTrue();
        verify(mongoRepository).existsByEmail(TestFixtures.EMAIL);
    }
}
