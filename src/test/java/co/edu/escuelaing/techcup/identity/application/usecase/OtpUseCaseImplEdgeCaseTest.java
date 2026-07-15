package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.exception.AccountBlockedException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidOtpException;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpUseCaseImplEdgeCaseTest {

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

    @InjectMocks
    private OtpUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "maxAttempts", 3);
        ReflectionTestUtils.setField(useCase, "otpExpirationMinutes", 5);
        ReflectionTestUtils.setField(useCase, "resendCooldownSeconds", 60);
    }

    @Test
    void validateOtpTreatsAlphanumericCodeAsIncorrectAndIncrementsAttempts() {
        OtpToken otp = TestFixtures.validOtp();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, "12A45B"))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Incorrect OTP code")
                .hasMessageContaining("Attempts remaining: 2");

        assertThat(otp.getFailedAttempts()).isEqualTo(1);
        verify(otpRepository).save(otp);
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void validateOtpTreatsBlankCodeAsIncorrectAtDomainBoundary() {
        OtpToken otp = TestFixtures.validOtp();
        otp.setFailedAttempts(1);
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, "   "))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Attempts remaining: 1");

        assertThat(otp.getFailedAttempts()).isEqualTo(2);
        verify(otpRepository).save(otp);
    }

    @Test
    void validateOtpBlocksImmediatelyWhenFailedAttemptsAreGreaterThanConfiguredMaximum() {
        OtpToken otp = TestFixtures.validOtp();
        otp.setFailedAttempts(99);
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(AccountBlockedException.class);

        assertThat(otp.isUsed()).isFalse();
        verify(otpRepository, never()).save(any());
        verify(jwtUtil, never()).generateToken(any(), any(), any());
    }

    @Test
    void validateOtpChecksExpirationBeforeUsedFlag() {
        OtpToken otp = TestFixtures.expiredOtp();
        otp.markAsUsed();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> useCase.validateOtp(TestFixtures.USER_ID, TestFixtures.OTP_CODE))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("expired");

        verify(otpRepository, never()).save(any());
    }

    @Test
    void resendOtpAllowsRequestExactlyAfterCooldownBoundary() {
        OtpToken oldOtp = TestFixtures.validOtp();
        oldOtp.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(61));
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(oldOtp));
        when(otpUtil.generateOtp()).thenReturn("000001");

        useCase.resendOtp(TestFixtures.USER_ID);

        verify(otpRepository).deleteAllByUserId(TestFixtures.USER_ID);
        ArgumentCaptor<OtpToken> captor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("000001");
        assertThat(captor.getValue().getExpiresAt()).isAfter(captor.getValue().getCreatedAt());
    }

    @Test
    void resendOtpRejectsEvenExpiredOtpWhenItWasCreatedInsideCooldownWindow() {
        OtpToken oldOtp = TestFixtures.expiredOtp();
        oldOtp.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(5));
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(oldOtp));

        assertThatThrownBy(() -> useCase.resendOtp(TestFixtures.USER_ID))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Please wait");

        verify(otpRepository, never()).deleteAllByUserId(any());
        verify(emailPort, never()).sendOtp(any(), any());
    }

    @Test
    void resendOtpRejectsOtpCreatedInTheFutureAsCooldownViolation() {
        OtpToken futureOtp = TestFixtures.validOtp();
        futureOtp.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30));
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(TestFixtures.activeUser()));
        when(otpRepository.findLatestByUserId(TestFixtures.USER_ID)).thenReturn(Optional.of(futureOtp));

        assertThatThrownBy(() -> useCase.resendOtp(TestFixtures.USER_ID))
                .isInstanceOf(InvalidOtpException.class)
                .hasMessageContaining("Please wait");

        verify(otpRepository, never()).save(any());
    }
}
