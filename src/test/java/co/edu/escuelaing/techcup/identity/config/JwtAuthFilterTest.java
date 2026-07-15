package co.edu.escuelaing.techcup.identity.config;

import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.TokenBlacklistService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del filtro de autenticacion JWT.
 * Verifica los tres caminos del filtro: sin header, token invalido y token valido.
 * SCRUM-15: Implementar gestion de sesion y validacion JWT.
 */
class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserDetailsServiceImpl userDetailsService;
    private TokenBlacklistService tokenBlacklistService;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsServiceImpl.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService, tokenBlacklistService);
        SecurityContextHolder.clearContext();
    }

    /**
     * SCRUM-15: Verifica que sin header Authorization el filtro deja pasar la solicitud.
     */
    @Test
    void doFilterInternal_noAuthHeader_continuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * SCRUM-15: Verifica que con header invalido (sin Bearer) el filtro deja pasar la solicitud.
     */
    @Test
    void doFilterInternal_invalidHeader_continuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic sometoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * SCRUM-15: Verifica que con token valido se autentica al usuario en el SecurityContext.
     */
    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.extractEmail("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@test.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * SCRUM-15: Verifica que con token invalido no se autentica al usuario.
     */
    @Test
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.extractEmail("invalid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * SCRUM-18: Verifica que un token revocado (logout) no autentica al usuario
     * aunque su firma y expiracion sigan siendo validas.
     */
    @Test
    void doFilterInternal_revokedToken_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer revoked-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("user@test.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.extractEmail("revoked-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("revoked-token", userDetails)).thenReturn(true);
        when(tokenBlacklistService.isRevoked("revoked-token")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}