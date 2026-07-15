package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidationUseCaseImplTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RevokedTokenRepositoryPort revokedTokenRepository;

    @InjectMocks
    private TokenValidationUseCaseImpl useCase;

    @Test
    void validateTokenReturnsUserWhenJwtIsValidAndNotRevoked() {
        User user = TestFixtures.activeUser();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = useCase.validateToken(TestFixtures.JWT);

        assertThat(result).isSameAs(user);
    }

    @Test
    void validateTokenRejectsInvalidJwt() {
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(false);

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalid or expired");

        verify(revokedTokenRepository, never()).existsByToken(TestFixtures.JWT);
    }

    @Test
    void validateTokenRejectsRevokedJwt() {
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(true);

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("revoked");

        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void validateTokenRejectsWhenUserNoLongerExists() {
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn("missing");
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
