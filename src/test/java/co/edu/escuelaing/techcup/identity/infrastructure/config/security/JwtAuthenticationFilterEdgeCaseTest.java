package co.edu.escuelaing.techcup.identity.infrastructure.config.security;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterEdgeCaseTest {

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
    void doFilterIgnoresLowercaseBearerPrefix() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = requestWithAuthorization("bearer " + TestFixtures.JWT);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).isTokenValid(TestFixtures.JWT);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterDoesNotAuthenticateBlankBearerToken() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = requestWithAuthorization("Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid("")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil).isTokenValid("");
        verify(revokedTokenRepository, never()).existsByToken("");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterPassesWhitespaceTokenToJwtValidatorAndDoesNotAuthenticateWhenInvalid() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = requestWithAuthorization("Bearer    ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid("   ")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil).isTokenValid("   ");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenRevokedRepositoryThrows() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = requestWithAuthorization("Bearer " + TestFixtures.JWT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenThrow(new IllegalStateException("database down"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenSessionActivityRepositoryThrows() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = requestWithAuthorization("Bearer " + TestFixtures.JWT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenThrow(new IllegalStateException("database down"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterClearsContextWhenRoleExtractionThrowsAfterTokenIsValid() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = requestWithAuthorization("Bearer " + TestFixtures.JWT);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.isTokenValid(TestFixtures.JWT)).thenReturn(true);
        when(revokedTokenRepository.existsByToken(TestFixtures.JWT)).thenReturn(false);
        when(sessionActivityRepository.findByToken(TestFixtures.JWT)).thenReturn(java.util.Optional.of(TestFixtures.sessionActivity()));
        when(jwtUtil.extractUserId(TestFixtures.JWT)).thenReturn(TestFixtures.USER_ID);
        when(jwtUtil.extractRole(TestFixtures.JWT)).thenThrow(new IllegalArgumentException("missing role"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterKeepsExistingAuthenticationUntouchedWhenHeaderIsMissing() throws Exception {
        JwtAuthenticationFilter filter = newFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).isTokenValid(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    private JwtAuthenticationFilter newFilter() {
        return new JwtAuthenticationFilter(jwtUtil, revokedTokenRepository, sessionActivityRepository, INACTIVITY_TIMEOUT_MINUTES);
    }

    private MockHttpServletRequest requestWithAuthorization(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", value);
        return request;
    }
}
