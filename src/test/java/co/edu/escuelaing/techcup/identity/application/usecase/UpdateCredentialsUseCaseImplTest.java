package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCredentialsUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;

    @InjectMocks
    private UpdateCredentialsUseCaseImpl useCase;

    @Test
    void updateRoleChangesRoleAndAudits() {
        User user = TestFixtures.activeUser();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));

        useCase.updateRole(TestFixtures.USER_ID, UserRole.CAPTAIN);

        assertThat(user.getRole()).isEqualTo(UserRole.CAPTAIN);
        assertThat(user.getUpdatedAt()).isNotNull();
        verify(userRepository).save(user);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(captor.capture());
        assertThat(captor.getValue().getActionType()).isEqualTo(AuditActionType.ROLE_UPDATED);
        assertThat(captor.getValue().getDescription()).contains("PLAYER").contains("CAPTAIN");
    }

    @Test
    void updateRoleThrowsWhenUserNotFound() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateRole("non-existent", UserRole.CAPTAIN))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    void updateStatusChangesStatusAndAudits() {
        User user = TestFixtures.activeUser();
        when(userRepository.findById(TestFixtures.USER_ID)).thenReturn(Optional.of(user));

        useCase.updateStatus(TestFixtures.USER_ID, AccountStatus.INACTIVE);

        assertThat(user.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        verify(userRepository).save(user);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(captor.capture());
        assertThat(captor.getValue().getActionType()).isEqualTo(AuditActionType.STATUS_UPDATED);
        assertThat(captor.getValue().getDescription()).contains("ACTIVE").contains("INACTIVE");
    }

    @Test
    void updateStatusThrowsWhenUserNotFound() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateStatus("non-existent", AccountStatus.INACTIVE))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}
