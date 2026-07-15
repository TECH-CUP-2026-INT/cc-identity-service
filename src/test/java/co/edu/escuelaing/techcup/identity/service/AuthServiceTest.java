package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.IdType;
import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.document.UserDocument.UserType;
import co.edu.escuelaing.techcup.identity.dto.*;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    private UserDocument user;

    @BeforeEach
    void setUp() {
        user = new UserDocument.Builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .password("hashedPassword")
            .firstName("John")
            .lastName("Doe")
            .enabled(true)
            .build();
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyOtp_success() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        authService.verifyOtp(request);

        verify(otpService).verify("123456", user);
        verify(userRepository).save(user);
        assertTrue(user.isEnabled());
    }

    @Test
    void verifyOtp_userNotFound_throwsException() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail("notfound@example.com");
        request.setCode("123456");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.verifyOtp(request));
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        UserDetails userDetails = mock(UserDetails.class);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refreshToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        UserDetails userDetails = mock(UserDetails.class);

        when(jwtService.extractEmail("valid-refresh-token")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-refresh-token", userDetails)).thenReturn(true);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
    }

    @Test
    void refreshToken_invalidToken_throwsException() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        UserDetails userDetails = mock(UserDetails.class);

        when(jwtService.extractEmail("invalid-token")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(request));
    }

    @Test
    void requestPasswordRecovery_existingEnabledUser_sendsOtp() {
        PasswordRecoveryRequest request = new PasswordRecoveryRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        ApiResponse response = authService.requestPasswordRecovery(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(otpService).generateAndSend(user);
    }

    @Test
    void requestPasswordRecovery_nonExistingUser_returnsGenericResponse() {
        PasswordRecoveryRequest request = new PasswordRecoveryRequest();
        request.setEmail("notfound@example.com");

        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        ApiResponse response = authService.requestPasswordRecovery(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(otpService, never()).generateAndSend(any(UserDocument.class));
    }

    @Test
    void requestPasswordRecovery_disabledUser_doesNotSendOtp() {
        PasswordRecoveryRequest request = new PasswordRecoveryRequest();
        request.setEmail("test@example.com");

        user.setEnabled(false);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        ApiResponse response = authService.requestPasswordRecovery(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(otpService, never()).generateAndSend(any(UserDocument.class));
    }

    @Test
    void resetPassword_success() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("newHashedPassword");
        when(userRepository.save(user)).thenReturn(user);

        ApiResponse response = authService.resetPassword(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("newHashedPassword", user.getPassword());

        verify(otpService).verify("123456", user);
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_userNotFound_throwsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("notfound@example.com");
        request.setCode("123456");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> authService.resetPassword(request)
        );

        verify(otpService, never()).verify(anyString(), any(UserDocument.class));
        verify(userRepository, never()).save(any(UserDocument.class));
    }

    @Test
    void resetPassword_disabledUser_throwsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("test@example.com");
        request.setCode("123456");
        request.setNewPassword("newPassword123");

        user.setEnabled(false);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                RuntimeException.class,
                () -> authService.resetPassword(request)
        );

        verify(otpService, never()).verify(anyString(), any(UserDocument.class));
        verify(userRepository, never()).save(any(UserDocument.class));
    }

    @Test
    void resetPassword_invalidOtp_throwsException() {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("test@example.com");
        request.setCode("000000");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        doThrow(new RuntimeException("Invalid or expired OTP code"))
                .when(otpService).verify("000000", user);

        assertThrows(
                RuntimeException.class,
                () -> authService.resetPassword(request)
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserDocument.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    // TC-01/02/03 — Registration flows
    // ══════════════════════════════════════════════════════════════════════

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
    void setupRegistrationMocks() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByIdNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(UserDocument.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(otpService).generateAndSend(any(UserDocument.class));
    }

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
            verify(userRepository).save(argThat(u ->
                    u.getUserType() == UserType.STUDENT &&
                            u.getRole() == UserDocument.Role.PLAYER &&
                            !u.isEnabled()
            ));
            verify(otpService).generateAndSend(any(UserDocument.class));
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

    @Nested
    @DisplayName("TC-02 — Guest Registration")
    class GuestRegistrationTests {

        private final UUID studentUuid = UUID.randomUUID();
        private final String studentId = studentUuid.toString();

        @BeforeEach
        void setupStudentMock() {
            UserDocument student = UserDocument.builder()
                    .id(studentUuid)
                    .email("student@escuelaing.edu.co")
                    .userType(UserType.STUDENT)
                    .build();
            when(userRepository.findByIdAndUserType(studentUuid, UserType.STUDENT))
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
            verify(userRepository).save(argThat(u ->
                    u.getUserType() == UserType.GUEST &&
                            u.getAssociatedStudentId().equals(studentId) &&
                            u.getRelationship().equals("Father") &&
                            u.getRole() == UserDocument.Role.PLAYER
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
            String nonExistentId = UUID.randomUUID().toString();

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
            verify(userRepository).save(argThat(u ->
                    u.getUserType() == UserType.GRADUATE &&
                            u.getRole() == UserDocument.Role.PLAYER
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
            verify(userRepository).save(argThat(u ->
                    u.getUserType() == UserType.GRADUATE &&
                            u.getEmail().endsWith("@gmail.com")
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
            verify(userRepository).save(argThat(u ->
                    u.getAssociatedStudentId() == null
            ));
        }
    }
}
