package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.ApiResponse;
import co.edu.escuelaing.techcup.identity.dto.RegisterRequest;
import co.edu.escuelaing.techcup.identity.entity.IdType;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.entity.UserEntity.UserType;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService registration flows.
 *
 * Tests covered:
 *   TC-01 — Student registration
 *   TC-02 — Guest registration
 *   TC-03 — Graduate registration
 *
 * Dependencies are mocked with Mockito — no Spring context or DB required.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService — Registration Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository         userRepository;
    @Mock private PasswordEncoder        passwordEncoder;
    @Mock private JwtService             jwtService;
    @Mock private OtpService             otpService;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    // ── Shared setup ──────────────────────────────────────────────────────

    private RegisterRequest baseRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Carlos");
        req.setLastName("Rojas");
        req.setPassword("securePass123");
        req.setIdType(IdType.CC);
        req.setIdNumber("1234567890");
        req.setDateOfBirth(LocalDate.of(2000, 5, 15));
        return req;
    }

    @BeforeEach
    void setupCommonMocks() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByIdNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(otpService).generateAndSend(any(UserEntity.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-01 — Student Registration
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-01 — Student Registration")
    class StudentRegistrationTests {

        @Test
        @DisplayName("Should register student successfully with institutional email")
        void shouldRegisterStudentSuccessfully() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@mail.escuelaing.edu.co");
            req.setUserType(UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            ApiResponse response = authService.register(req);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("Registration successful");
            verify(userRepository).save(argThat(user ->
                    user.getUserType() == UserType.STUDENT &&
                            user.getRole() == UserEntity.Role.PLAYER &&
                            !user.isEnabled()
            ));
            verify(otpService).generateAndSend(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should fail when student uses Gmail instead of institutional email")
        void shouldFailWhenStudentUsesGmail() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@gmail.com");
            req.setUserType(UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("institutional email");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when student does not provide academic program")
        void shouldFailWhenStudentMissesAcademicProgram() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@mail.escuelaing.edu.co");
            req.setUserType(UserType.STUDENT);
            req.setSemester(4);
            req.setAcademicProgram(null);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Academic program");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when student does not provide semester")
        void shouldFailWhenStudentMissesSemester() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@mail.escuelaing.edu.co");
            req.setUserType(UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            // semester intentionally null

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Semester");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when email is already registered")
        void shouldFailWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("c.rojas@escuelaing.edu.co")).thenReturn(true);

            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@escuelaing.edu.co");
            req.setUserType(UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Email already registered");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when ID number is already registered")
        void shouldFailWhenIdNumberAlreadyExists() {
            when(userRepository.existsByIdNumber("1234567890")).thenReturn(true);

            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@escuelaing.edu.co");
            req.setUserType(UserType.STUDENT);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            req.setSemester(4);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("ID number already registered");

            verify(userRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-02 — Guest Registration
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-02 — Guest Registration")
    class GuestRegistrationTests {

        private final UUID studentId = UUID.randomUUID();

        @BeforeEach
        void setupStudentMock() {
            UserEntity student = UserEntity.builder()
                    .id(studentId)
                    .email("student@escuelaing.edu.co")
                    .userType(UserType.STUDENT)
                    .build();
            when(userRepository.findByIdAndUserType(studentId, UserType.STUDENT))
                    .thenReturn(Optional.of(student));
        }

        @Test
        @DisplayName("Should register guest successfully with Gmail and valid student association")
        void shouldRegisterGuestSuccessfully() {
            RegisterRequest req = baseRequest();
            req.setEmail("familiarojas@gmail.com");
            req.setUserType(UserType.GUEST);
            req.setAssociatedStudentId(studentId);
            req.setRelationship("Father");

            ApiResponse response = authService.register(req);

            assertThat(response.isSuccess()).isTrue();
            verify(userRepository).save(argThat(user ->
                    user.getUserType() == UserType.GUEST &&
                            user.getAssociatedStudentId().equals(studentId) &&
                            user.getRelationship().equals("Father") &&
                            user.getRole() == UserEntity.Role.PLAYER
            ));
        }

        @Test
        @DisplayName("Should fail when guest uses institutional email instead of Gmail")
        void shouldFailWhenGuestUsesInstitutionalEmail() {
            RegisterRequest req = baseRequest();
            req.setEmail("familiar@escuelaing.edu.co");
            req.setUserType(UserType.GUEST);
            req.setAssociatedStudentId(studentId);
            req.setRelationship("Father");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Gmail");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when guest does not provide associated student ID")
        void shouldFailWhenGuestMissesAssociatedStudent() {
            RegisterRequest req = baseRequest();
            req.setEmail("familiarojas@gmail.com");
            req.setUserType(UserType.GUEST);
            req.setRelationship("Father");
            // associatedStudentId intentionally null

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("associated student");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when associated student does not exist in DB")
        void shouldFailWhenAssociatedStudentNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.findByIdAndUserType(nonExistentId, UserType.STUDENT))
                    .thenReturn(Optional.empty());

            RegisterRequest req = baseRequest();
            req.setEmail("familiarojas@gmail.com");
            req.setUserType(UserType.GUEST);
            req.setAssociatedStudentId(nonExistentId);
            req.setRelationship("Father");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("associated student was not found");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when guest does not provide relationship")
        void shouldFailWhenGuestMissesRelationship() {
            RegisterRequest req = baseRequest();
            req.setEmail("familiarojas@gmail.com");
            req.setUserType(UserType.GUEST);
            req.setAssociatedStudentId(studentId);
            // relationship intentionally null

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Relationship");

            verify(userRepository, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-03 — Graduate Registration
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TC-03 — Graduate Registration")
    class GraduateRegistrationTests {

        @Test
        @DisplayName("Should register graduate successfully with institutional email")
        void shouldRegisterGraduateWithInstitutionalEmail() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@escuelaing.edu.co");
            req.setUserType(UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");

            ApiResponse response = authService.register(req);

            assertThat(response.isSuccess()).isTrue();
            verify(userRepository).save(argThat(user ->
                    user.getUserType() == UserType.GRADUATE &&
                            user.getRole() == UserEntity.Role.PLAYER
            ));
        }

        @Test
        @DisplayName("Should register graduate successfully with Gmail when no institutional email")
        void shouldRegisterGraduateWithGmail() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@gmail.com");
            req.setUserType(UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");

            ApiResponse response = authService.register(req);

            assertThat(response.isSuccess()).isTrue();
            verify(userRepository).save(argThat(user ->
                    user.getUserType() == UserType.GRADUATE &&
                            user.getEmail().endsWith("@gmail.com")
            ));
        }

        @Test
        @DisplayName("Should fail when graduate uses an unsupported email domain")
        void shouldFailWhenGraduateUsesUnsupportedEmail() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@outlook.com");
            req.setUserType(UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("institutional email or a Gmail");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when graduate does not provide academic program")
        void shouldFailWhenGraduateMissesAcademicProgram() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@escuelaing.edu.co");
            req.setUserType(UserType.GRADUATE);
            // academicProgram intentionally null

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Academic program");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Graduate should not require student association unlike guest")
        void graduateShouldNotRequireStudentAssociation() {
            RegisterRequest req = baseRequest();
            req.setEmail("c.rojas@gmail.com");
            req.setUserType(UserType.GRADUATE);
            req.setAcademicProgram("INGENIERIA_DE_SISTEMAS");
            // associatedStudentId intentionally null — should be fine

            ApiResponse response = authService.register(req);

            assertThat(response.isSuccess()).isTrue();
            verify(userRepository).save(argThat(user ->
                    user.getAssociatedStudentId() == null
            ));
        }
    }
}