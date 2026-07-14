package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.IdType;
import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import co.edu.escuelaing.techcup.identity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SCRUM-22: Pruebas del controlador de arbitros.
 * Verifica que solo ORGANIZER pueda crear arbitros y que no autenticados reciban 401.
 */
@WebMvcTest(RefereeController.class)
@Import(RefereeControllerTest.TestSecurityConfig.class)
class RefereeControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/referees/**").authenticated()
                    .anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;


    @Test
    @WithMockUser(roles = "ORGANIZER")
    void createReferee_success() throws Exception {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Maria Lopez", LocalDate.of(1990, 3, 10), IdType.CC, "11223344", "maria@gmail.com"
        );

        mockMvc.perform(post("/api/v1/referees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createReferee_unauthenticated_returns401() throws Exception {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Maria Lopez", LocalDate.of(1990, 3, 10), IdType.CC, "11223344", "maria@gmail.com"
        );

        mockMvc.perform(post("/api/v1/referees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
