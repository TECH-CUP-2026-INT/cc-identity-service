package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeUserSessionsUseCaseImplTest {

    @Mock
    private SessionActivityRepositoryPort sessionActivityRepository;
    @Mock
    private RevokedTokenRepositoryPort revokedTokenRepository;
    @Mock
    private AuditEventRepositoryPort auditRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private Claims claims;

    @InjectMocks
    private RevokeUserSessionsUseCaseImpl useCase;

    @Test
    void revokesEveryActiveSessionAndAudits() {
        SessionActivity sessionA = SessionActivity.builder().token("jwt-a").userId(TestFixtures.USER_ID).build();
        SessionActivity sessionB = SessionActivity.builder().token("jwt-b").userId(TestFixtures.USER_ID).build();
        when(sessionActivityRepository.findAllByUserId(TestFixtures.USER_ID)).thenReturn(List.of(sessionA, sessionB));
        when(revokedTokenRepository.existsByToken(any())).thenReturn(false);
        when(jwtUtil.extractClaims(any())).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(3600)));

        useCase.revokeAllSessions(TestFixtures.USER_ID);

        ArgumentCaptor<RevokedToken> revokedCaptor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository, times(2)).save(revokedCaptor.capture());
        assertThat(revokedCaptor.getAllValues())
                .extracting(RevokedToken::getToken)
                .containsExactlyInAnyOrder("jwt-a", "jwt-b");

        verify(sessionActivityRepository).deleteByToken("jwt-a");
        verify(sessionActivityRepository).deleteByToken("jwt-b");

        ArgumentCaptor<AuditEvent> auditCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo(AuditActionType.ACCOUNT_DISABLED);
        assertThat(auditCaptor.getValue().isSuccess()).isTrue();
    }

    @Test
    void doesNothingWhenUserHasNoActiveSessions() {
        when(sessionActivityRepository.findAllByUserId(TestFixtures.USER_ID)).thenReturn(List.of());

        useCase.revokeAllSessions(TestFixtures.USER_ID);

        verify(revokedTokenRepository, never()).save(any());
        verify(auditRepository).save(any(AuditEvent.class));
    }

    @Test
    void skipsSessionAlreadyRevokedButStillDeletesActivityRecord() {
        SessionActivity session = SessionActivity.builder().token("jwt-a").userId(TestFixtures.USER_ID).build();
        when(sessionActivityRepository.findAllByUserId(TestFixtures.USER_ID)).thenReturn(List.of(session));
        when(revokedTokenRepository.existsByToken("jwt-a")).thenReturn(true);

        useCase.revokeAllSessions(TestFixtures.USER_ID);

        verify(revokedTokenRepository, never()).save(any());
        verify(sessionActivityRepository).deleteByToken("jwt-a");
    }

    @Test
    void skipsTokenWithUnparsableClaimsButStillDeletesActivityRecord() {
        SessionActivity session = SessionActivity.builder().token("bad-jwt").userId(TestFixtures.USER_ID).build();
        when(sessionActivityRepository.findAllByUserId(TestFixtures.USER_ID)).thenReturn(List.of(session));
        when(revokedTokenRepository.existsByToken("bad-jwt")).thenReturn(false);
        when(jwtUtil.extractClaims("bad-jwt")).thenThrow(new IllegalArgumentException("invalid"));

        useCase.revokeAllSessions(TestFixtures.USER_ID);

        verify(revokedTokenRepository, never()).save(any());
        verify(sessionActivityRepository).deleteByToken("bad-jwt");
    }
}
