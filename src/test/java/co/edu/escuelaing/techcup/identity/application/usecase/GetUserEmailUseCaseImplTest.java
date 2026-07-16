package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserEmailUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private GetUserEmailUseCaseImpl useCase;

    @Test
    void getEmailByUserIdReturnsEmailWhenUserExists() {
        User user = TestFixtures.activeUser();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));

        String email = useCase.getEmailByUserId(TestFixtures.USER_ID);

        assertThat(email).isEqualTo(TestFixtures.EMAIL);
    }

    @Test
    void getEmailByUserIdThrowsWhenUserNotFound() {
        UUID missingUserId = UUID.randomUUID();
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getEmailByUserId(missingUserId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
