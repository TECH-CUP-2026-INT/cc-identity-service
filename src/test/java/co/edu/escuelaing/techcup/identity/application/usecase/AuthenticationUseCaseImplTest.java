package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountInactiveException;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountLockedException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidCredentialsException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.model.UserProfileSnapshot;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.GoogleOAuthPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserProfilePort;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private OtpRepositoryPort otpRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private EmailPort emailPort;
    @Mock
    private GoogleOAuthPort googleOAuthPort;
    @Mock
    private UserProfilePort userProfilePort;
    @Mock
    private PasswordUtil passwordUtil;
    @Mock
    private OtpUtil otpUtil;

    @InjectMocks
    private AuthenticationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(useCase, "maxFailedLoginAttempts", 3);
        ReflectionTestUtils.setField(useCase, "lockoutDurationMinutes", 15);
    }

    @Test
    void loginWithInstitutionalEmailSendsOtpAndReturnsUserId() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches(TestFixtures.PASSWORD, user.getPassword())).thenReturn(true);
        when(otpUtil.generateOtp()).thenReturn(TestFixtures.OTP_CODE);

        UUID userId = useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD);

        assertThat(userId).isEqualTo(TestFixtures.USER_ID);
        verify(otpRepository).deleteAllByUserId(TestFixtures.USER_ID);
        verify(emailPort).sendOtp(user.getEmail(), TestFixtures.OTP_CODE);

        ArgumentCaptor<OtpToken> otpCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(otpCaptor.capture());
        assertThat(otpCaptor.getValue().getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(otpCaptor.getValue().getCode()).isEqualTo(TestFixtures.OTP_CODE);
        assertThat(otpCaptor.getValue().isUsed()).isFalse();
        assertThat(otpCaptor.getValue().getExpiresAt()).isAfter(otpCaptor.getValue().getCreatedAt());

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.OTP_SENT);
        assertThat(auditCaptor.getValue().isSuccess()).isTrue();
    }

    @Test
    void loginWithInstitutionalEmailFailsWhenUsersPlayersServiceIsUnavailable() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenThrow(new co.edu.escuelaing.techcup.identity.domain.exception.UserProfileUnavailableException(user.getId().toString()));

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD))
                .isInstanceOf(co.edu.escuelaing.techcup.identity.domain.exception.UserProfileUnavailableException.class);

        verify(otpRepository, never()).save(any());
        verify(emailPort, never()).sendOtp(any(), any());
    }

    @Test
    void loginWithInstitutionalEmailRejectsUnknownEmailWithoutAuditing() {
        when(userRepository.findByEmail(TestFixtures.EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(TestFixtures.EMAIL, TestFixtures.PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(otpRepository, auditRepository, emailPort);
    }

    @Test
    void loginWithInstitutionalEmailRejectsInactiveAccountAndAuditsFailure() {
        User user = TestFixtures.inactiveUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.INACTIVE));

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD))
                .isInstanceOf(AccountInactiveException.class);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.USER_LOGIN_FAILED);
        assertThat(auditCaptor.getValue().getDescription()).isEqualTo("Account inactive");
        assertThat(auditCaptor.getValue().isSuccess()).isFalse();
        verify(otpRepository, never()).save(any());
    }

    @Test
    void loginWithInstitutionalEmailRejectsInvalidPasswordAndAuditsFailure() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getDescription()).isEqualTo("Invalid password");
        verify(otpRepository, never()).save(any());
        verify(emailPort, never()).sendOtp(any(), any());
    }

    @Test
    void loginWithInstitutionalEmailLocksAccountAfterMaxFailedAttempts() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getStatus()).isEqualTo(AccountStatus.LOCKED);

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD))
                .isInstanceOf(AccountLockedException.class);
        verify(otpRepository, never()).save(any());
    }

    @Test
    void loginWithInstitutionalEmailRejectsAttemptWhileAccountIsLocked() {
        User user = TestFixtures.lockedUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD))
                .isInstanceOf(AccountLockedException.class);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getDescription()).isEqualTo("Account locked due to multiple failed attempts");
        verify(passwordUtil, never()).matches(any(), any());
    }

    @Test
    void loginWithInstitutionalEmailAutoUnlocksWhenLockWindowHasExpired() {
        User user = TestFixtures.lockedUser();
        user.setLockedUntil(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(1));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches(TestFixtures.PASSWORD, user.getPassword())).thenReturn(true);
        when(otpUtil.generateOtp()).thenReturn(TestFixtures.OTP_CODE);

        UUID userId = useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD);

        assertThat(userId).isEqualTo(TestFixtures.USER_ID);
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getFailedLoginAttempts()).isZero();
        verify(otpRepository).save(any(OtpToken.class));
    }

    @Test
    void loginWithInstitutionalEmailResetsFailedAttemptsOnSuccessfulLogin() {
        User user = TestFixtures.activeUser();
        user.setFailedLoginAttempts(2);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches(TestFixtures.PASSWORD, user.getPassword())).thenReturn(true);
        when(otpUtil.generateOtp()).thenReturn(TestFixtures.OTP_CODE);

        useCase.loginWithInstitutionalEmail(user.getEmail(), TestFixtures.PASSWORD);

        assertThat(user.getFailedLoginAttempts()).isZero();
        verify(userRepository).save(user);
    }

    @Test
    void loginWithGmailRejectsAttemptWhileAccountIsLocked() {
        User user = TestFixtures.lockedUser();
        when(googleOAuthPort.validateGoogleToken("google-token"))
                .thenReturn(Map.of("email", user.getEmail()));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.loginWithGmail("google-token"))
                .isInstanceOf(AccountLockedException.class);
        verify(otpRepository, never()).save(any());
    }

    @Test
    void loginWithGmailValidatesGoogleTokenAndSendsOtp() {
        User user = TestFixtures.activeUser();
        when(googleOAuthPort.validateGoogleToken("google-token"))
                .thenReturn(Map.of("email", user.getEmail(), "name", user.getFullName()));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(otpUtil.generateOtp()).thenReturn(TestFixtures.OTP_CODE);

        UUID userId = useCase.loginWithGmail("google-token");

        assertThat(userId).isEqualTo(TestFixtures.USER_ID);
        verify(otpRepository).deleteAllByUserId(TestFixtures.USER_ID);
        verify(otpRepository).save(any(OtpToken.class));
        verify(emailPort).sendOtp(user.getEmail(), TestFixtures.OTP_CODE);
        verify(auditRepository).save(any(AuditEvent.class));
    }

    @Test
    void loginWithGmailRejectsUnknownUser() {
        when(googleOAuthPort.validateGoogleToken("google-token"))
                .thenReturn(Map.of("email", "missing@gmail.com"));
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.loginWithGmail("google-token"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("missing@gmail.com");

        verify(otpRepository, never()).save(any());
    }

    @Test
    void loginWithGmailRejectsInactiveUserAndAuditsFailure() {
        User user = TestFixtures.inactiveUser();
        when(googleOAuthPort.validateGoogleToken("google-token"))
                .thenReturn(Map.of("email", user.getEmail()));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.INACTIVE));

        assertThatThrownBy(() -> useCase.loginWithGmail("google-token"))
                .isInstanceOf(AccountInactiveException.class);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.USER_LOGIN_FAILED);
        assertThat(auditCaptor.getValue().getDescription()).isEqualTo("Account inactive");
    }
}
