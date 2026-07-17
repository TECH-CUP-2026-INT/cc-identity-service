package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountInactiveException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenValidationUseCaseImplTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RevokedTokenRepositoryPort revokedTokenRepository;
    @Mock
    private SessionActivityRepositoryPort sessionActivityRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;

    @InjectMocks
    private TokenValidationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "inactivityTimeoutMinutes", 30);
    }

    @Test
    void validateTokenReturnsUserWhenJwtIsValidAndNotRevoked() {
        User user = TestFixtures.activeUser();
        SessionActivity activity = TestFixtures.sessionActivity();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtUtil.extractRole(TestFixtures.JWT)).thenReturn(user.getRole().name());

        User result = useCase.validateToken(TestFixtures.JWT);

        assertThat(result).isSameAs(user);
        verify(sessionActivityRepository).save(activity);
    }

    @Test
    void validateTokenReturnsRoleFromJwtInsteadOfStaleLocalCopy() {
        User user = TestFixtures.activeUser();
        user.setRole(UserRole.PLAYER);
        SessionActivity activity = TestFixtures.sessionActivity();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtUtil.extractRole(TestFixtures.JWT)).thenReturn("CAPTAIN");

        User result = useCase.validateToken(TestFixtures.JWT);

        assertThat(result.getRole()).isEqualTo(UserRole.CAPTAIN);
    }

    @Test
    void validateTokenRejectsInvalidJwt() {
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(false);

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalid or expired");

        verify(revokedTokenRepository, never()).existsByToken(TestFixtures.JWT);
    }

    @Test
    void validateTokenRejectsRevokedJwt() {
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(true);

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("revoked");

        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any(UUID.class));
        verify(sessionActivityRepository, never()).findByToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void validateTokenRejectsWhenAccountWasDisabled() {
        User user = TestFixtures.inactiveUser();
        SessionActivity activity = TestFixtures.sessionActivity();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(AccountInactiveException.class);

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.ACCOUNT_DISABLED);
        assertThat(auditCaptor.getValue().isSuccess()).isFalse();
    }

    @Test
    void validateTokenRejectsWhenUserNoLongerExists() {
        UUID missingUserId = UUID.randomUUID();
        SessionActivity activity = TestFixtures.sessionActivity();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(missingUserId);
        when(userRepository.findById(missingUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(missingUserId.toString());
    }

    @Test
    void validateTokenRejectsWhenNoSessionActivityRecordExists() {
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Session not found");

        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any(UUID.class));
    }

    @Test
    void validateTokenRejectsAndDeletesSessionExpiredByInactivity() {
        SessionActivity activity = TestFixtures.sessionActivity();
        activity.setLastActivityAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(31));
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));

        assertThatThrownBy(() -> useCase.validateToken(TestFixtures.JWT))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("inactivity");

        verify(sessionActivityRepository).deleteByToken(TestFixtures.JWT);
        verify(sessionActivityRepository, never()).save(org.mockito.ArgumentMatchers.any(SessionActivity.class));
        verify(userRepository, never()).findById(org.mockito.ArgumentMatchers.any(UUID.class));

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.SESSION_EXPIRED);
        assertThat(auditCaptor.getValue().isSuccess()).isFalse();
    }
}
