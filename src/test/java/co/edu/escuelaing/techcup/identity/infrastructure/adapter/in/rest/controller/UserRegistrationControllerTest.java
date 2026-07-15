package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.RegisterUserUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateAdminOrganizerRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserRegistrationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;
    @MockBean
    private UserMapper userMapper;

    @Test
    void createAdminOrOrganizerReturnsCreatedUser() throws Exception {
        CreateAdminOrganizerRequest request = CreateAdminOrganizerRequest.builder()
                .fullName("Grace Hopper")
                .email("organizer@escuelaing.edu.co")
                .password(TestFixtures.PASSWORD)
                .userType(UserType.ORGANIZER)
                .build();
        User mappedUser = TestFixtures.organizerWithoutRole();
        User savedUser = TestFixtures.activeUser();
        savedUser.setEmail(request.getEmail());
        when(userMapper.toDomain(org.mockito.ArgumentMatchers.any(CreateAdminOrganizerRequest.class))).thenReturn(mappedUser);
        when(registerUserUseCase.createAdminOrOrganizer(mappedUser)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(TestFixtures.userResponse());

        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TestFixtures.USER_ID))
                .andExpect(jsonPath("$.email").value(TestFixtures.EMAIL));
    }

    @Test
    void createAdminOrOrganizerValidationErrorsReturnBadRequest() throws Exception {
        CreateAdminOrganizerRequest request = CreateAdminOrganizerRequest.builder()
                .fullName("")
                .email("not-an-email")
                .password("")
                .userType(null)
                .build();

        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.userType").exists());
    }

    @Test
    void createAdminOrOrganizerDuplicateEmailReturnsConflict() throws Exception {
        CreateAdminOrganizerRequest request = CreateAdminOrganizerRequest.builder()
                .fullName("Grace Hopper")
                .email("organizer@escuelaing.edu.co")
                .password(TestFixtures.PASSWORD)
                .userType(UserType.ORGANIZER)
                .build();
        User mappedUser = TestFixtures.organizerWithoutRole();
        when(userMapper.toDomain(org.mockito.ArgumentMatchers.any(CreateAdminOrganizerRequest.class))).thenReturn(mappedUser);
        when(registerUserUseCase.createAdminOrOrganizer(mappedUser))
                .thenThrow(new UserAlreadyExistsException(request.getEmail()));

        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_EXISTS"));
    }
}
