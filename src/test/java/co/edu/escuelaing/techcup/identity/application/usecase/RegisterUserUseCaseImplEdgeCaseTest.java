package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.DomainException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.PasswordUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplEdgeCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private RegisterUserUseCaseImpl useCase;

    @Test
    void createAdminOrOrganizerRejectsStudentUserTypeInsteadOfCoercingIt() {
        User input = User.builder()
                .email("student@escuelaing.edu.co")
                .password(TestFixtures.PASSWORD)
                .userType(UserType.STUDENT)
                .build();
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> useCase.createAdminOrOrganizer(input))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("User type must be ADMIN or ORGANIZER");

        verify(passwordUtil, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    void createAdminOrOrganizerRejectsNullUserTypeBeforeEncodingPassword() {
        User input = User.builder()
                .email("user@escuelaing.edu.co")
                .password(TestFixtures.PASSWORD)
                .userType(null)
                .build();
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> useCase.createAdminOrOrganizer(input))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("User type must be ADMIN or ORGANIZER");

        verify(passwordUtil, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}
