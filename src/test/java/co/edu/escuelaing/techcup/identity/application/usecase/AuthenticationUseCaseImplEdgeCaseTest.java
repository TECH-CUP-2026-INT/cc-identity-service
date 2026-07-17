package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidCredentialsException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseImplEdgeCaseTest {

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
    }

    @Test
    void institutionalLoginRejectsBlankPasswordWhenPasswordEncoderDoesNotMatch() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(TestFixtures.EMAIL)).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches("   ", TestFixtures.ENCODED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(TestFixtures.EMAIL, "   "))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(otpRepository, never()).save(any());
        verify(emailPort, never()).sendOtp(any(), any());
        verify(auditRepository).save(any());
    }

    @Test
    void institutionalLoginPropagatesRepositoryLookupUsingEmailExactlyAsReceived() {
        when(userRepository.findByEmail(" STUDENT@ESCUELAING.EDU.CO ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.loginWithInstitutionalEmail(" STUDENT@ESCUELAING.EDU.CO ", TestFixtures.PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository).findByEmail(" STUDENT@ESCUELAING.EDU.CO ");
        verifyNoInteractions(otpRepository, auditRepository, emailPort);
    }

    @Test
    void gmailLoginRejectsTokenPayloadWithoutEmailClaim() {
        Map<String, String> googlePayload = new HashMap<>();
        googlePayload.put("name", "No Email User");
        when(googleOAuthPort.validateGoogleToken("google-token-without-email")).thenReturn(googlePayload);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.loginWithGmail("google-token-without-email"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("null");

        verify(userRepository).findByEmail(null);
        verify(otpRepository, never()).save(any());
        verify(emailPort, never()).sendOtp(any(), any());
    }

    @Test
    void successfulLoginCreatesOtpWithConfiguredExpirationWindow() {
        User user = TestFixtures.activeUser();
        when(userRepository.findByEmail(TestFixtures.EMAIL)).thenReturn(Optional.of(user));
        when(userProfilePort.fetchProfile(user.getId()))
                .thenReturn(new UserProfileSnapshot(user.getRole(), AccountStatus.ACTIVE));
        when(passwordUtil.matches(TestFixtures.PASSWORD, TestFixtures.ENCODED_PASSWORD)).thenReturn(true);
        when(otpUtil.generateOtp()).thenReturn("000123");

        useCase.loginWithInstitutionalEmail(TestFixtures.EMAIL, TestFixtures.PASSWORD);

        ArgumentCaptor<OtpToken> captor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("000123");
        assertThat(captor.getValue().getExpiresAt()).isAfter(captor.getValue().getCreatedAt().plusMinutes(4));
        assertThat(captor.getValue().getExpiresAt()).isBefore(captor.getValue().getCreatedAt().plusMinutes(6));
    }
}
