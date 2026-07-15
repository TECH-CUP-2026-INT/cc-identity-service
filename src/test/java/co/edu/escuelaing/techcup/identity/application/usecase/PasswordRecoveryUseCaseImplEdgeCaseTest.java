package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.RecoveryCodeExpiredException;
import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RecoveryTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.OtpUtil;
import co.edu.escuelaing.techcup.identity.shared.util.PasswordUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryUseCaseImplEdgeCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RecoveryTokenRepositoryPort recoveryTokenRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private EmailPort emailPort;
    @Mock
    private OtpUtil otpUtil;
    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private PasswordRecoveryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "recoveryExpirationMinutes", 15);
    }

    @Test
    void requestRecoveryDoesNothingForNullEmailBecauseExistenceIsNotRevealed() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        useCase.requestRecovery(null);

        verifyNoInteractions(recoveryTokenRepository, auditRepository, emailPort, otpUtil);
    }

    @Test
    void requestRecoveryDoesNothingForBlankEmailBecauseExistenceIsNotRevealed() {
        when(userRepository.findByEmail("   ")).thenReturn(Optional.empty());

        useCase.requestRecovery("   ");

        verifyNoInteractions(recoveryTokenRepository, auditRepository, emailPort, otpUtil);
    }

    @Test
    void resetPasswordDoesNotMarkUsedTokenAgainAndDoesNotUpdatePassword() {
        User user = TestFixtures.activeUser();
        RecoveryToken token = TestFixtures.validRecoveryToken();
        token.markAsUsed();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), token.getCode(), "NewPassword123!"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid recovery code");

        assertThat(token.isUsed()).isTrue();
        verify(recoveryTokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(passwordUtil, never()).encode(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    void resetPasswordDoesNotConsumeTokenWhenRecoveryCodeIsWrong() {
        User user = TestFixtures.activeUser();
        RecoveryToken token = TestFixtures.validRecoveryToken();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), "WRONG", "NewPassword123!"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid recovery code");

        assertThat(token.isUsed()).isFalse();
        verify(recoveryTokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(passwordUtil, never()).encode(any());
    }

    @Test
    void resetPasswordDoesNotConsumeExpiredTokenEvenWhenCodeMatches() {
        User user = TestFixtures.activeUser();
        RecoveryToken token = TestFixtures.expiredRecoveryToken();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), token.getCode(), "NewPassword123!"))
                .isInstanceOf(RecoveryCodeExpiredException.class);

        assertThat(token.isUsed()).isFalse();
        verify(recoveryTokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
