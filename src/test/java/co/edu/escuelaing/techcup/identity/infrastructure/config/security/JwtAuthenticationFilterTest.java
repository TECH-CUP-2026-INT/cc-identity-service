package co.edu.escuelaing.techcup.identity.infrastructure.config.security;

import co.edu.escuelaing.techcup.identity.domain.model.SessionActivity;
import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final int INACTIVITY_TIMEOUT_MINUTES = 30;

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RevokedTokenRepositoryPort revokedTokenRepository;
    @Mock
    private SessionActivityRepositoryPort sessionActivityRepository;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterContinuesWithoutAuthenticationWhenHeaderIsMissing() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterContinuesWithoutAuthenticationWhenHeaderDoesNotStartWithBearer() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterAuthenticatesValidNonRevokedBearerTokenWithActiveSession() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionActivity activity = TestFixtures.sessionActivity();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(TestFixtures.USER_ID);
        when(jwtUtil.extractRole(TestFixtures.JWT)).thenReturn("ADMIN");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(TestFixtures.USER_ID);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        verify(sessionActivityRepository).save(activity);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterDoesNotAuthenticateRevokedToken() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(sessionActivityRepository, never()).findByToken(TestFixtures.JWT);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterDoesNotAuthenticateWhenNoSessionActivityRecordExists() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.empty());

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterDoesNotAuthenticateAndDeletesSessionExpiredByInactivity() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SessionActivity activity = TestFixtures.sessionActivity();
        activity.setLastActivityAt(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(31));
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(Optional.of(activity));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(sessionActivityRepository).deleteByToken(TestFixtures.JWT);
        verify(sessionActivityRepository, never()).save(org.mockito.ArgumentMatchers.any(SessionActivity.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenJwtProcessingThrows() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = bearerRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenThrow(new IllegalArgumentException("bad token"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private JwtAuthenticationFilter newFilter() {
        return new JwtAuthenticationFilter(jwtUtil, revokedTokenRepository, sessionActivityRepository, INACTIVITY_TIMEOUT_MINUTES);
    }

    private MockHttpServletRequest bearerRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TestFixtures.JWT);
        return request;
    }
}
