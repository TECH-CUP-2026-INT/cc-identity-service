package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.RecoveryCodeExpiredException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
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
import org.mockito.ArgumentCaptor;
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
class PasswordRecoveryUseCaseImplTest {

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
    void requestRecoveryCreatesTokenSendsEmailAndAuditsWhenUserExists() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpUtil.generateRecoveryCode()).thenReturn("ABCD1234");

        useCase.requestRecovery(user.getEmail());

        verify(recoveryTokenRepository).deleteAllByUserId(user.getId());
        ArgumentCaptor<RecoveryToken> tokenCaptor = ArgumentCaptor.forClass(RecoveryToken.class);
        verify(recoveryTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getUserId()).isEqualTo(user.getId());
        assertThat(tokenCaptor.getValue().getCode()).isEqualTo("ABCD1234");
        assertThat(tokenCaptor.getValue().isUsed()).isFalse();
        assertThat(tokenCaptor.getValue().getExpiresAt()).isAfter(tokenCaptor.getValue().getCreatedAt());

        verify(emailPort).sendRecoveryCode(user.getEmail(), "ABCD1234");
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.PASSWORD_RECOVERY_REQUESTED);
    }

    @Test
    void requestRecoveryDoesNotRevealMissingEmail() {
        when(userRepository.findByEmail("missing@escuelaing.edu.co")).thenReturn(Optional.empty());

        useCase.requestRecovery("missing@escuelaing.edu.co");

        verifyNoInteractions(recoveryTokenRepository, auditRepository, emailPort, otpUtil);
    }

    @Test
    void resetPasswordMarksRecoveryTokenUsedUpdatesPasswordAndAudits() {
        User user = TestFixtures.activeUser();
        RecoveryToken token = TestFixtures.validRecoveryToken();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId())).thenReturn(Optional.of(token));
        when(passwordUtil.encode("NewPassword123!")).thenReturn("new-encoded-password");

        useCase.resetPassword(user.getEmail(), "ABCD1234", "NewPassword123!");

        assertThat(token.isUsed()).isTrue();
        verify(recoveryTokenRepository).save(token);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("new-encoded-password");
        assertThat(userCaptor.getValue().getUpdatedAt()).isNotNull();

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.PASSWORD_RESET);
        assertThat(auditCaptor.getValue().isSuccess()).isTrue();
    }

    @Test
    void resetPasswordRejectsUnknownEmail() {
        when(userRepository.findByEmail("missing@escuelaing.edu.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.resetPassword("missing@escuelaing.edu.co", "ABCD1234", "NewPassword123!"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid recovery request");
    }

    @Test
    void resetPasswordRejectsWhenNoRecoveryCodeExists() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), "ABCD1234", "NewPassword123!"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("No recovery code found");
    }

    @Test
    void resetPasswordRejectsExpiredRecoveryCode() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId()))
                .thenReturn(Optional.of(TestFixtures.expiredRecoveryToken()));

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), "ABCD1234", "NewPassword123!"))
                .isInstanceOf(RecoveryCodeExpiredException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordRejectsUsedRecoveryCode() {
        User user = TestFixtures.activeUser();
        RecoveryToken token = TestFixtures.validRecoveryToken();
        token.markAsUsed();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), "ABCD1234", "NewPassword123!"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid recovery code");
    }

    @Test
    void resetPasswordRejectsWrongRecoveryCode() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(recoveryTokenRepository.findLatestByUserId(user.getId()))
                .thenReturn(Optional.of(TestFixtures.validRecoveryToken()));

        assertThatThrownBy(() -> useCase.resetPassword(user.getEmail(), "WRONG999", "NewPassword123!"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid recovery code");
    }
}
