package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.dto.RegisterRequest;
import co.edu.escuelaing.techcup.identity.document.IdType;
import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.document.UserDocument.UserType;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import co.edu.escuelaing.techcup.identity.service.OtpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the registration endpoints.
 * Loads the full Spring context with H2 in-memory database.
 * OtpService is mocked to avoid actual email sending during tests.
 *
 * Tests covered:
 *   TC-01 — POST /api/auth/register (STUDENT)
 *   TC-02 — POST /api/auth/register (GUEST)
 *   TC-03 — POST /api/auth/register (GRADUATE)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController — Registration Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc         mockMvc;
    @Autowired private UserRepository  userRepository;
    @MockBean  private OtpService      otpService;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        doNothing().when(otpService).generateAndSend(any(UserDocument.class));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private RegisterRequest baseRequest(String email, UserType userType) {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Carlos");
        req.setLastName("Rojas");
        req.setEmail(email);
        req.setPassword("securePass123");
        req.setUserType(userType);
        req.setIdType(IdType.CC);
        req.setIdNumber("1234567890");
        req.setDateOfBirth(LocalDate.of(2000, 5, 15));
        return req;
    }

    private String toJson(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-01 — Student Registration
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-01 — POST /api/auth/register (STUDENT)")
    class StudentIntegrationTests {

        @Test
        @Order(1)
        @DisplayName("Should return 200 when student registers with institutional email")
        void shouldReturn200ForValidStudent() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@mail.escuelaing.edu.co", UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(
                            containsString("Check your email for the OTP")));
        }

        @Test
        @Order(2)
        @DisplayName("Should return 400 when student uses Gmail")
        void shouldReturn400WhenStudentUsesGmail() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@gmail.com", UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            containsString("institutional email")));
        }

        @Test
        @Order(3)
        @DisplayName("Should return 400 when student email is already registered")
        void shouldReturn400WhenStudentEmailDuplicated() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@mail.escuelaing.edu.co", UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            // First registration
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk());

            // Duplicate registration
            req.setIdNumber("9999999999");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            containsString("Email already registered")));
        }

        @Test
        @Order(4)
        @DisplayName("Should return 400 when student missing semester")
        void shouldReturn400WhenStudentMissesSemester() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@mail.escuelaing.edu.co", UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            // semester null

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-02 — Guest Registration
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-02 — POST /api/auth/register (GUEST)")
    class GuestIntegrationTests {

        private String savedStudentId;

        @BeforeEach
        void createStudent() {
            UserDocument student = UserDocument.builder()
                    .email("student@escuelaing.edu.co")
                    .password("encoded")
                    .firstName("Ana")
                    .lastName("Gomez")
                    .userType(UserType.STUDENT)
                    .role(UserDocument.Role.PLAYER)
                    .idType(IdType.CC)
                    .idNumber("9876543210")
                    .dateOfBirth(LocalDate.of(2001, 3, 10))
                    .academicProgram("INGENIERIA_DE_SISTEMAS")
                    .semester(3)
                    .enabled(true)
                    .build();
            savedStudentId = userRepository.save(student).getId().toString();
        }

        @Test
        @Order(1)
        @DisplayName("Should return 200 when guest registers with Gmail and valid student")
        void shouldReturn200ForValidGuest() throws Exception {
            RegisterRequest req = baseRequest("familiarojas@gmail.com", UserType.GUEST);
            req.setAssociatedStudentId(savedStudentId);
            req.setRelationship("Father");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(2)
        @DisplayName("Should return 400 when guest uses institutional email")
        void shouldReturn400WhenGuestUsesInstitutionalEmail() throws Exception {
            RegisterRequest req = baseRequest("familiar@escuelaing.edu.co", UserType.GUEST);
            req.setAssociatedStudentId(savedStudentId);
            req.setRelationship("Father");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            containsString("Gmail")));
        }

        @Test
        @Order(3)
        @DisplayName("Should return 400 when associated student does not exist")
        void shouldReturn400WhenStudentNotFound() throws Exception {
            RegisterRequest req = baseRequest("familiarojas@gmail.com", UserType.GUEST);
            req.setAssociatedStudentId(UUID.randomUUID().toString());
            req.setRelationship("Mother");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            containsString("associated student was not found")));
        }

        @Test
        @Order(4)
        @DisplayName("Should return 400 when guest does not provide relationship")
        void shouldReturn400WhenGuestMissesRelationship() throws Exception {
            RegisterRequest req = baseRequest("familiarojas@gmail.com", UserType.GUEST);
            req.setAssociatedStudentId(savedStudentId);
            // relationship null

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            containsString("Relationship")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-03 — Graduate Registration
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-03 — POST /api/auth/register (GRADUATE)")
    class GraduateIntegrationTests {

        @Test
        @Order(1)
        @DisplayName("Should return 200 when graduate registers with institutional email")
        void shouldReturn200WithInstitutionalEmail() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@escuelaing.edu.co", UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(2)
        @DisplayName("Should return 200 when graduate registers with Gmail")
        void shouldReturn200WithGmail() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@gmail.com", UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @Order(3)
        @DisplayName("Should return 400 when graduate uses unsupported email domain")
        void shouldReturn400WithUnsupportedDomain() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@outlook.com", UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            containsString("institutional email or a Gmail")));
        }

        @Test
        @Order(4)
        @DisplayName("Should return 400 when graduate missing academic program")
        void shouldReturn400WhenMissingAcademicProgram() throws Exception {
            RegisterRequest req = baseRequest("c.rojas@gmail.com", UserType.GRADUATE);
            // academicProgram null

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            containsString("Academic program")));
        }
    }
}