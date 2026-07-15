package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.PasswordUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private RegisterUserUseCaseImpl useCase;

    @Test
    void createAdminOrOrganizerAssignsAdminRoleWhenUserTypeIsAdmin() {
        User input = User.builder()
                .fullName("Admin User")
                .email("admin@escuelaing.edu.co")
                .password(TestFixtures.PASSWORD)
                .userType(UserType.ADMIN)
                .build();
        User saved = TestFixtures.adminUser();
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(passwordUtil.encode(TestFixtures.PASSWORD)).thenReturn(TestFixtures.ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = useCase.createAdminOrOrganizer(input);

        assertThat(result).isSameAs(saved);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserType()).isEqualTo(UserType.ADMIN);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(userCaptor.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(TestFixtures.ENCODED_PASSWORD);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.USER_REGISTERED);
    }

    @Test
    void createAdminOrOrganizerCoercesNonAdminToOrganizerRole() {
        User input = TestFixtures.organizerWithoutRole();
        User saved = TestFixtures.organizerWithoutRole();
        saved.setId("organizer-1");
        saved.setUserType(UserType.ORGANIZER);
        saved.setRole(UserRole.ORGANIZER);
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(passwordUtil.encode(TestFixtures.PASSWORD)).thenReturn(TestFixtures.ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        useCase.createAdminOrOrganizer(input);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserType()).isEqualTo(UserType.ORGANIZER);
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.ORGANIZER);
    }

    @Test
    void createAdminOrOrganizerRejectsDuplicateEmail() {
        User input = TestFixtures.organizerWithoutRole();
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> useCase.createAdminOrOrganizer(input))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(input.getEmail());

        verify(userRepository, never()).save(any());
        verify(auditRepository, never()).save(any());
    }
}
