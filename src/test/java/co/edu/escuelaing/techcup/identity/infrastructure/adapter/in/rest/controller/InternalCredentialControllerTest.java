package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.GetUserEmailUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.RevokeUserSessionsUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateCredentialRequest;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InternalCredentialController.class,
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
class InternalCredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateCredentialsUseCase createCredentialsUseCase;
    @MockBean
    private GetUserEmailUseCase getUserEmailUseCase;
    @MockBean
    private RevokeUserSessionsUseCase revokeUserSessionsUseCase;
    @MockBean
    private UserMapper userMapper;

    @Test
    void createCredentialsReturnsCreatedUser() throws Exception {
        CreateCredentialRequest request = validRequest();
        User savedUser = TestFixtures.activeUser();
        when(createCredentialsUseCase.createCredentials(
                request.getUserId(), request.getEmail(), request.getPassword(), request.getFullName(), request.getUserType(), request.getRole()))
                .thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(TestFixtures.userResponse());

        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TestFixtures.USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(TestFixtures.EMAIL));

        verify(createCredentialsUseCase).createCredentials(
                request.getUserId(), request.getEmail(), request.getPassword(), request.getFullName(), request.getUserType(), request.getRole());
    }

    @Test
    void createCredentialsValidationErrorsReturnBadRequest() throws Exception {
        CreateCredentialRequest request = CreateCredentialRequest.builder()
                .userId(null)
                .email("bad-email")
                .password("")
                .fullName("")
                .userType(null)
                .role(null)
                .build();

        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.userId").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.userType").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    void createCredentialsDuplicateEmailReturnsConflict() throws Exception {
        CreateCredentialRequest request = validRequest();
        when(createCredentialsUseCase.createCredentials(
                request.getUserId(), request.getEmail(), request.getPassword(), request.getFullName(), request.getUserType(), request.getRole()))
                .thenThrow(new UserAlreadyExistsException(request.getEmail()));

        mockMvc.perform(post("/api/v1/internal/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void getUserEmailReturnsEmail() throws Exception {
        when(getUserEmailUseCase.getEmailByUserId(TestFixtures.USER_ID)).thenReturn(TestFixtures.EMAIL);

        mockMvc.perform(get("/api/v1/internal/credentials/{userId}/email", TestFixtures.USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TestFixtures.EMAIL));

        verify(getUserEmailUseCase).getEmailByUserId(TestFixtures.USER_ID);
    }

    @Test
    void getUserEmailReturnsNotFoundWhenUserMissing() throws Exception {
        java.util.UUID missingUserId = java.util.UUID.randomUUID();
        when(getUserEmailUseCase.getEmailByUserId(missingUserId))
                .thenThrow(new UserNotFoundException(missingUserId.toString()));

        mockMvc.perform(get("/api/v1/internal/credentials/{userId}/email", missingUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }

    @Test
    void revokeSessionsCallsUseCaseAndReturnsOk() throws Exception {
        mockMvc.perform(post("/api/v1/internal/credentials/{userId}/revoke-sessions", TestFixtures.USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sessions revoked for user " + TestFixtures.USER_ID));

        verify(revokeUserSessionsUseCase).revokeAllSessions(TestFixtures.USER_ID);
    }

    private CreateCredentialRequest validRequest() {
        return CreateCredentialRequest.builder()
                .userId(TestFixtures.USER_ID)
                .email(TestFixtures.EMAIL)
                .password(TestFixtures.PASSWORD)
                .fullName("Ada Lovelace")
                .userType(UserType.STUDENT)
                .role(UserRole.PLAYER)
                .build();
    }
}
