package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditQueryUseCaseImplTest {

    @Mock
    private AuditEventRepositoryPort auditRepository;

    @InjectMocks
    private AuditQueryUseCaseImpl useCase;

    @Test
    void queryEventsDelegatesFiltersToRepository() {
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 1, 31, 23, 59);
        List<AuditEvent> expected = List.of(TestFixtures.auditEvent());
        when(auditRepository.findByFilters(startDate, endDate, AuditActionType.USER_LOGIN, "user-1"))
                .thenReturn(expected);

        List<AuditEvent> result = useCase.queryEvents(startDate, endDate, AuditActionType.USER_LOGIN, "user-1");

        assertThat(result).isSameAs(expected);
        verify(auditRepository).findByFilters(startDate, endDate, AuditActionType.USER_LOGIN, "user-1");
    }
}
