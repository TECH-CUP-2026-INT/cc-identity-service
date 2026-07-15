package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.port.in.RegisterUserUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler.GlobalExceptionHandler;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
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

import static org.mockito.Mockito.verifyNoInteractions;
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
class UserRegistrationControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;
    @MockBean
    private UserMapper userMapper;

    @Test
    void createAdminOrOrganizerRejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").value("Full name is required"))
                .andExpect(jsonPath("$.errors.email").value("Email is required"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"))
                .andExpect(jsonPath("$.errors.userType").value("User type is required (ADMIN or ORGANIZER)"));

        verifyNoInteractions(registerUserUseCase, userMapper);
    }

    @Test
    void createAdminOrOrganizerRejectsInvalidEmailAndBlankFields() throws Exception {
        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName":" ",
                                  "email":"not-an-email",
                                  "password":"   ",
                                  "userType":"ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").value("Full name is required"))
                .andExpect(jsonPath("$.errors.email").value("Invalid email format"))
                .andExpect(jsonPath("$.errors.password").value("Password is required"));

        verifyNoInteractions(registerUserUseCase, userMapper);
    }

    @Test
    void createAdminOrOrganizerRejectsStudentTypeAtDomainBoundary() throws Exception {
        when(userMapper.toDomain(org.mockito.ArgumentMatchers.any(co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateAdminOrganizerRequest.class)))
                .thenReturn(co.edu.escuelaing.techcup.identity.domain.model.User.builder()
                        .email("admin@escuelaing.edu.co")
                        .password("Password123!")
                        .userType(co.edu.escuelaing.techcup.identity.domain.enums.UserType.STUDENT)
                        .build());
        when(registerUserUseCase.createAdminOrOrganizer(org.mockito.ArgumentMatchers.any(co.edu.escuelaing.techcup.identity.domain.model.User.class)))
                .thenThrow(new co.edu.escuelaing.techcup.identity.domain.exception.DomainException(
                        "INVALID_USER_TYPE", "User type must be ADMIN or ORGANIZER"));

        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName":"Ada Lovelace",
                                  "email":"admin@escuelaing.edu.co",
                                  "password":"Password123!",
                                  "userType":"STUDENT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_USER_TYPE"));
    }

    @Test
    void createAdminOrOrganizerRejectsUnknownEnumValuesAsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName":"Ada Lovelace",
                                  "email":"admin@escuelaing.edu.co",
                                  "password":"Password123!",
                                  "userType":"UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));

        verifyNoInteractions(registerUserUseCase, userMapper);
    }

    @Test
    void createAdminOrOrganizerRejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/v1/register/admin-organizer")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));

        verifyNoInteractions(registerUserUseCase, userMapper);
    }
}
