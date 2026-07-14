package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import co.edu.escuelaing.techcup.identity.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias del controlador de gestion de usuarios.
 * Verifica la inhabilitacion de usuarios y el control de acceso por rol.
 * SCRUM-61: Inhabilitar usuario.
 */
@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    /**
     * SCRUM-61: Verifica que un ADMIN puede inhabilitar un usuario exitosamente.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void disableUser_success() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).disableUser(any());

        mockMvc.perform(patch("/api/users/" + userId + "/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User disabled successfully"));
    }

    /**
     * SCRUM-61: Verifica que una solicitud sin autenticacion retorna 401.
     */
    @Test
    void disableUser_unauthenticated_returns401() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/users/" + userId + "/disable"))
                .andExpect(status().isUnauthorized());
    }
}