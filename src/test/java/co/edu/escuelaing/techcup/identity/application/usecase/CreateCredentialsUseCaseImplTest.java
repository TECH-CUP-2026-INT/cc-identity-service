package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.OtpRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCredentialsUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private OtpRepositoryPort otpRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private EmailPort emailPort;
    @Mock
    private PasswordUtil passwordUtil;
    @Mock
    private OtpUtil otpUtil;

    @InjectMocks
    private CreateCredentialsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "otpExpirationMinutes", 5);
    }

    @Test
    void createCredentialsPersistsActiveUserSendsOtpAndAudits() {
        User savedUser = TestFixtures.activeUser();
        when(userRepository.existsByEmail(TestFixtures.EMAIL)).thenReturn(false);
        when(passwordUtil.encode(TestFixtures.PASSWORD)).thenReturn(TestFixtures.ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(otpUtil.generateOtp()).thenReturn(TestFixtures.OTP_CODE);

        User result = useCase.createCredentials(
                TestFixtures.USER_ID,
                TestFixtures.EMAIL,
                TestFixtures.PASSWORD,
                "Ada Lovelace",
                UserType.STUDENT,
                UserRole.PLAYER
        );

        assertThat(result).isSameAs(savedUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(TestFixtures.EMAIL);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(TestFixtures.ENCODED_PASSWORD);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(userCaptor.getValue().getCreatedAt()).isNotNull();
        assertThat(userCaptor.getValue().getUpdatedAt()).isNotNull();

        ArgumentCaptor<OtpToken> otpCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(otpCaptor.capture());
        assertThat(otpCaptor.getValue().getUserId()).isEqualTo(savedUser.getId());
        assertThat(otpCaptor.getValue().getCode()).isEqualTo(TestFixtures.OTP_CODE);
        verify(emailPort).sendOtp(savedUser.getEmail(), TestFixtures.OTP_CODE);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.CREDENTIALS_CREATED);
        assertThat(auditCaptor.getValue().getDescription()).contains(UserType.STUDENT.name());
    }

    @Test
    void createCredentialsRejectsDuplicateEmail() {
        when(userRepository.existsByEmail(TestFixtures.EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> useCase.createCredentials(
                TestFixtures.USER_ID,
                TestFixtures.EMAIL,
                TestFixtures.PASSWORD,
                "Ada Lovelace",
                UserType.STUDENT,
                UserRole.PLAYER
        ))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(TestFixtures.EMAIL);

        verify(userRepository, never()).save(any());
        verify(otpRepository, never()).save(any());
        verify(emailPort, never()).sendOtp(any(), any());
        verify(auditRepository, never()).save(any());
    }
}
