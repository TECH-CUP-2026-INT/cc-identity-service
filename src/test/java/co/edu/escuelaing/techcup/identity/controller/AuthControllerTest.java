package co.edu.escuelaing.techcup.identity.controller;

<<<<<<< HEAD
import co.edu.escuelaing.techcup.identity.dto.*;
=======
import co.edu.escuelaing.techcup.identity.dto.AuthResponse;
import co.edu.escuelaing.techcup.identity.dto.LoginRequest;
import co.edu.escuelaing.techcup.identity.dto.OtpVerifyRequest;
import co.edu.escuelaing.techcup.identity.dto.RefreshTokenRequest;
import co.edu.escuelaing.techcup.identity.dto.RegisterRequest;
>>>>>>> origin/develop
import co.edu.escuelaing.techcup.identity.service.AuthService;
import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
<<<<<<< HEAD
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SCRUM-13 y SCRUM-14: Pruebas del controlador de autenticacion.
 * Verifica registro, verificacion de OTP, login y refresh de token.
=======
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias del controlador de autenticacion.
 * Cubre los endpoints de registro, verificacion OTP, login y refresco de token.
 * SCRUM-13: registro y verificacion OTP.
 * SCRUM-14: login.
 * SCRUM-15: refresco de token JWT.
>>>>>>> origin/develop
 */
@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
<<<<<<< HEAD
    private UserDetailsServiceImpl userDetailsServiceImpl;


    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@gmail.com");
        request.setPassword("Password1!");
=======
    private UserDetailsServiceImpl userDetailsService;

    /**
     * SCRUM-13: Verifica que el registro de usuario retorna 200 con mensaje de exito.
     */
    @Test
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");
>>>>>>> origin/develop
        request.setFirstName("Juan");
        request.setLastName("Perez");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
<<<<<<< HEAD
    }
=======
}
>>>>>>> origin/develop

    @Test
    void verifyOtp_success() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest();
<<<<<<< HEAD
        request.setEmail("user@gmail.com");
=======
        request.setEmail("user@test.com");
>>>>>>> origin/develop
        request.setCode("123456");

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
<<<<<<< HEAD

    @Test
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@gmail.com");
        request.setPassword("Password1!");

        when(authService.login(any())).thenReturn(new AuthResponse("access", "refresh", "user@gmail.com", "USER"));
=======
    /**
     * SCRUM-14: Verifica que el login retorna 200 con tokens de acceso y refresco.
     */
    @Test
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");

        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token", "user@test.com", "USER");
        when(authService.login(any())).thenReturn(authResponse);
>>>>>>> origin/develop

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
<<<<<<< HEAD
                .andExpect(jsonPath("$.accessToken").value("access"));
    }

    @Test
    void refresh_success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("some-refresh-token");

        when(authService.refreshToken(any())).thenReturn(new AuthResponse("newAccess", "newRefresh", "user@gmail.com", "USER"));
=======
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    /**
     * SCRUM-15: Verifica que el refresco de token retorna 200 con nuevo token de acceso.
     */
    @Test
    void refresh_success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        AuthResponse authResponse = new AuthResponse("new-access-token", "valid-refresh-token", "user@test.com", "USER");
        when(authService.refreshToken(any())).thenReturn(authResponse);
>>>>>>> origin/develop

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
<<<<<<< HEAD
                .andExpect(jsonPath("$.accessToken").value("newAccess"));
    }
}
=======
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }
}
>>>>>>> origin/develop
