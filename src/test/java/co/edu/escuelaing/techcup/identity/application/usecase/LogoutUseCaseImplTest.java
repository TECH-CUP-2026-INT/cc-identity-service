package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseImplTest {

    @Mock
    private RevokedTokenRepositoryPort revokedTokenRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private Claims claims;

    @InjectMocks
    private LogoutUseCaseImpl useCase;

    @Test
    void logoutRevokesTokenAndAuditsWhenTokenIsValid() {
        Date expiration = Date.from(Instant.now().plusSeconds(3_600));
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(jwtUtil.extractClaims(TestFixtures.JWT)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(TestFixtures.USER_ID);
        when(claims.getExpiration()).thenReturn(expiration);

        useCase.logout(TestFixtures.JWT);

        ArgumentCaptor<RevokedToken> revokedCaptor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(revokedCaptor.capture());
        assertThat(revokedCaptor.getValue().getToken()).isEqualTo(TestFixtures.JWT);
        assertThat(revokedCaptor.getValue().getUserId()).isEqualTo(TestFixtures.USER_ID);
        assertThat(revokedCaptor.getValue().getExpiresAt()).isNotNull();

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.USER_LOGOUT);
        assertThat(auditCaptor.getValue().isSuccess()).isTrue();
    }

    @Test
    void logoutIsIdempotentWhenTokenIsAlreadyRevoked() {
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(true);

        useCase.logout(TestFixtures.JWT);

        verify(jwtUtil, never()).extractClaims(TestFixtures.JWT);
        verify(revokedTokenRepository, never()).save(org.mockito.ArgumentMatchers.any(RevokedToken.class));
        verify(auditRepository, never()).save(org.mockito.ArgumentMatchers.any(AuditEvent.class));
    }

    @Test
    void logoutTreatsInvalidTokenAsAlreadyClosedSession() {
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(jwtUtil.extractClaims(TestFixtures.JWT)).thenThrow(new IllegalArgumentException("invalid"));

        useCase.logout(TestFixtures.JWT);

        verify(revokedTokenRepository, never()).save(org.mockito.ArgumentMatchers.any(RevokedToken.class));
        verify(auditRepository, never()).save(org.mockito.ArgumentMatchers.any(AuditEvent.class));
    }
}
