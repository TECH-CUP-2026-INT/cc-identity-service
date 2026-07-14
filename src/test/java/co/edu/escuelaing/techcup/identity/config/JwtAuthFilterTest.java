package co.edu.escuelaing.techcup.identity.config;

import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
<<<<<<< HEAD
=======
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
>>>>>>> origin/develop
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

<<<<<<< HEAD
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * SCRUM-15: Pruebas unitarias del filtro JWT de autenticacion.
 * Verifica que el filtro procese correctamente el header Authorization
 * y establezca el contexto de seguridad cuando el token es valido.
=======
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del filtro de autenticacion JWT.
 * Verifica los tres caminos del filtro: sin header, token invalido y token valido.
 * SCRUM-15: Implementar gestion de sesion y validacion JWT.
>>>>>>> origin/develop
 */
class JwtAuthFilterTest {

    private JwtService jwtService;
    private UserDetailsServiceImpl userDetailsService;
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsServiceImpl.class);
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

<<<<<<< HEAD
    @Test
    void noAuthHeader_continuesFilterChain() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);

=======
    /**
     * SCRUM-15: Verifica que sin header Authorization el filtro deja pasar la solicitud.
     */
    @Test
    void doFilterInternal_noAuthHeader_continuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

>>>>>>> origin/develop
        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

<<<<<<< HEAD
    @Test
    void nonBearerHeader_continuesFilterChain() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

=======
    /**
     * SCRUM-15: Verifica que con header invalido (sin Bearer) el filtro deja pasar la solicitud.
     */
    @Test
    void doFilterInternal_invalidHeader_continuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic sometoken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

>>>>>>> origin/develop
        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

<<<<<<< HEAD
    @Test
    void validToken_setsAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("user@test.com", "pass", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(jwtService.extractEmail("valid.token.here")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid.token.here", userDetails)).thenReturn(true);
=======
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
>>>>>>> origin/develop

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
<<<<<<< HEAD
    }

    @Test
    void invalidToken_doesNotSetAuthentication() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("user@test.com", "pass", Collections.emptyList());

        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.extractEmail("bad.token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("bad.token", userDetails)).thenReturn(false);
=======
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
>>>>>>> origin/develop

        jwtAuthFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/develop
