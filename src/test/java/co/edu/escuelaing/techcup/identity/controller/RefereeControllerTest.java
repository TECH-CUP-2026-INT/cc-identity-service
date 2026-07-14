package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.IdType;
import co.edu.escuelaing.techcup.identity.service.JwtService;
import co.edu.escuelaing.techcup.identity.service.UserDetailsServiceImpl;
import co.edu.escuelaing.techcup.identity.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
<<<<<<< HEAD
=======
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
>>>>>>> origin/develop
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

<<<<<<< HEAD
=======
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
>>>>>>> origin/develop
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
<<<<<<< HEAD
 * SCRUM-22: Pruebas del controlador de arbitros.
 * Verifica que solo ORGANIZER pueda crear arbitros y que no autenticados reciban 401.
=======
 * Pruebas unitarias del controlador de arbitros.
 * Verifica la creacion de arbitros y el control de acceso por rol.
 * SCRUM-11: creacion de cuenta de arbitro.
>>>>>>> origin/develop
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
<<<<<<< HEAD
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/referees/**").authenticated()
                    .anyRequest().permitAll());
=======
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
>>>>>>> origin/develop
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

<<<<<<< HEAD
    @Autowired
    private ObjectMapper objectMapper;

=======
>>>>>>> origin/develop
    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
<<<<<<< HEAD
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
=======
    private UserDetailsServiceImpl userDetailsService;

    /**
     * SCRUM-11: Verifica que un ORGANIZER puede crear un arbitro exitosamente.
     */
    @Test
    @WithMockUser(roles = "ORGANIZER")
    void createReferee_success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(1995, 5, 10),
                IdType.CC, "12345678", "referee@gmail.com"
        );

        doNothing().when(userService).createReferee(any());

        mockMvc.perform(post("/api/v1/referees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    /**
     * SCRUM-11: Verifica que una solicitud sin autenticacion retorna 401.
     */
    @Test
    void createReferee_unauthenticated_returns401() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(1995, 5, 10),
                IdType.CC, "12345678", "referee@gmail.com"
>>>>>>> origin/develop
        );

        mockMvc.perform(post("/api/v1/referees")
                        .contentType(MediaType.APPLICATION_JSON)
<<<<<<< HEAD
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
=======
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
>>>>>>> origin/develop
