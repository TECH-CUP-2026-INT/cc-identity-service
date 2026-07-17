package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountBlockedException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidOtpException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.exception.UserProfileUnavailableException;
import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.model.UserProfileSnapshot;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserProfilePort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import co.edu.escuelaing.techcup.identity.shared.util.OtpUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpUseCaseImplTest {

    @Mock
    private OtpRepositoryPort otpRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private EmailPort emailPort;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private OtpUtil otpUtil;
    @Mock
    private SessionActivityRepositoryPort sessionActivityRepository;
    @Mock
    private UserProfilePort userProfilePort;

    @InjectMocks
    private OtpUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "maxAttempts", 3);
        ReflectionTestUtils.setField(useCase, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(useCase, "resendCooldownSeconds", 60);
    }

    @Test
    void validateOtpMarksTokenUsedGeneratesJwtAndAuditsLogin() {
        User user = TestFixtures.activeUser();
        OtpToken otp = TestFixtures.validOtp();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus.ACTIVE));
        when(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(TestFixtures.JWT);

        String jwt = useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE);

        assertThat(jwt).isEqualTo(TestFixtures.JWT);
        assertThat(otp.isUsed()).isTrue();
        verify(otpRepository).save(otp);

        ArgumentCaptor<SessionActivity> sessionCaptor = ArgumentCaptor.forClass(SessionActivity.class);
        verify(sessionActivityRepository).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getValue().getToken()).isEqualTo(TestFixtures.JWT);
        assertThat(sessionCaptor.getValue().getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(sessionCaptor.getValue().getLastActivityAt()).isNotNull();

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.USER_LOGIN);
        assertThat(auditCaptor.getValue().isSuccess()).isTrue();
    }

    @Test
    void validateOtpFailsWhenUsersPlayersServiceIsUnavailable() {
        User user = TestFixtures.activeUser();
        OtpToken otp = TestFixtures.validOtp();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenThrow(new UserProfileUnavailableException(user.getId().toString()));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(UserProfileUnavailableException.class);

        verify(jwtUtil, never()).generateToken(any(), any(), any());
        verify(sessionActivityRepository, never()).save(any());
    }

    @Test
    void validateOtpRejectsMissingUser() {
        UUID missingUserId = UUID.randomUUID();
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validateOtp(missingUserId, TestFixtures.OTP_CODE))
                .isInstanceOf(UserNotFoundException.class);

        verify(otpRepository, never()).findLatestByUserId(any());
    }

    @Test
    void validateOtpRejectsWhenNoOtpExists() {
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("No OTP found");
    }

    @Test
    void validateOtpBlocksWhenMaxAttemptsWereAlreadyReached() {
        OtpToken otp = TestFixtures.validOtp();
        otp.setFailedAttempts(3);
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(AccountBlockedException.class);

        verifyAuditFailure("Account blocked - max attempts exceeded");
        verify(otpRepository, never()).save(any());
    }

    @Test
    void validateOtpRejectsExpiredTokenAndAuditsFailure() {
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.expiredOtp()));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("expired");

        verifyAuditFailure("OTP expired");
    }

    @Test
    void validateOtpRejectsAlreadyUsedTokenWithoutAuditingAgain() {
        OtpToken otp = TestFixtures.validOtp();
        otp.markAsUsed();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("already been used");

        verify(auditRepository, never()).save(any());
    }

    @Test
    void validateOtpIncrementsAttemptsForWrongCodeAndReportsRemainingAttempts() {
        OtpToken otp = TestFixtures.validOtp();
        otp.setFailedAttempts(1);
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, "000000"))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Attempts remaining: 1");

        assertThat(otp.getFailedAttempts()).isEqualTo(2);
        verify(otpRepository).save(otp);
        verifyAuditFailure("Incorrect OTP code");
    }

    @Test
    void validateOtpBlocksWhenWrongCodeReachesMaxAttempts() {
        OtpToken otp = TestFixtures.validOtp();
        otp.setFailedAttempts(2);
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, "000000"))
                .isInstanceOf(AccountBlockedException.class);

        assertThat(otp.getFailedAttempts()).isEqualTo(3);
        verify(otpRepository).save(otp);
    }

    @Test
    void resendOtpDeletesExistingOtpCreatesNewOneAndSendsEmailAfterCooldown() {
        User user = TestFixtures.activeUser();
        OtpToken oldOtp = TestFixtures.validOtp();
        oldOtp.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(2));
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(oldOtp));
        when(otpUtil.generateOtp()).thenReturn("654321");

        useCase.resendOtp(TestFixtures.USER_ID);

        verify(otpRepository).deleteAllByUserId(TestFixtures.USER_ID);
        ArgumentCaptor<OtpToken> otpCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(otpCaptor.capture());
        assertThat(otpCaptor.getValue().getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(otpCaptor.getValue().getCode()).isEqualTo("654321");
        assertThat(otpCaptor.getValue().getFailedAttempts()).isZero();
        verify(emailPort).sendOtp(user.getEmail(), "654321");
    }

    @Test
    void resendOtpAllowsRequestWhenThereIsNoPreviousOtp() {
        User user = TestFixtures.activeUser();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.empty());
        when(otpUtil.generateOtp()).thenReturn("654321");

        useCase.resendOtp(TestFixtures.USER_ID);

        verify(otpRepository).deleteAllByUserId(TestFixtures.USER_ID);
        verify(otpRepository).save(any(OtpToken.class));
        verify(emailPort).sendOtp(user.getEmail(), "654321");
    }

    @Test
    void resendOtpRejectsRequestDuringCooldown() {
        OtpToken recentOtp = TestFixtures.validOtp();
        recentOtp.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(10));
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(recentOtp));

        assertThatThrownBy(() -> useCase.resendOtp(TestFixtures.USER_ID))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Please wait");

        verify(otpRepository, never()).deleteAllByUserId(any());
        verify(otpRepository, never()).save(any());
        verify(emailPort, never()).sendOtp(any(), any());
    }

    @Test
    void resendOtpRejectsMissingUser() {
        UUID missingUserId = UUID.randomUUID();
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.resendOtp(missingUserId))
                .isInstanceOf(UserNotFoundException.class);
    }

    private void verifyAuditFailure(String expectedDescription) {
        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.OTP_FAILED);
        assertThat(auditCaptor.getValue().getDescription()).isEqualTo(expectedDescription);
        assertThat(auditCaptor.getValue().isSuccess()).isFalse();
    }
}
