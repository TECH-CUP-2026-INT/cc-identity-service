package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.AuthResponse;
import co.edu.escuelaing.techcup.identity.dto.LoginRequest;
import co.edu.escuelaing.techcup.identity.dto.RegisterRequest;
import co.edu.escuelaing.techcup.identity.entity.User;
import co.edu.escuelaing.techcup.identity.entity.UserRole;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import co.edu.escuelaing.techcup.identity.util.JwtTokenProvider;
import co.edu.escuelaing.techcup.identity.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceExtendedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest adminRequest;
    private RegisterRequest organizerRequest;
    private RegisterRequest graduateRequest;
    private RegisterRequest guestRequest;
    private RegisterRequest studentRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        adminRequest = new RegisterRequest();
        adminRequest.setEmail("admin@techcup.com");
        adminRequest.setPassword("admin123");
        adminRequest.setName("Admin User");
        adminRequest.setRole(UserRole.ADMIN);

        organizerRequest = new RegisterRequest();
        organizerRequest.setEmail("organizer@techcup.com");
        organizerRequest.setPassword("org123");
        organizerRequest.setName("Organizer User");
        organizerRequest.setRole(UserRole.ORGANIZER);

        graduateRequest = new RegisterRequest();
        graduateRequest.setEmail("graduate@universidad.edu.co");
        graduateRequest.setPassword("grad123");
        graduateRequest.setName("Graduate User");
        graduateRequest.setRole(UserRole.GRADUATE);

        guestRequest = new RegisterRequest();
        guestRequest.setEmail("guest@gmail.com");
        guestRequest.setPassword("guest123");
        guestRequest.setName("Guest User");
        guestRequest.setRole(UserRole.GUEST);
        guestRequest.setAssociatedStudentId("student-123");

        studentRequest = new RegisterRequest();
        studentRequest.setEmail("student@universidad.edu.co");
        studentRequest.setPassword("student123");
        studentRequest.setName("Student User");
        studentRequest.setRole(UserRole.STUDENT);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("student@universidad.edu.co");
        loginRequest.setPassword("student123");

        user = User.builder()
                .id("123")
                .email("student@universidad.edu.co")
                .password("encoded")
                .name("Student User")
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();
    }

    @Test
    void register_Admin_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(adminRequest);

        assertNotNull(response);
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void register_Organizer_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(organizerRequest);

        assertNotNull(response);
    }

    @Test
    void register_GraduateWithUniversityEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(graduateRequest);

        assertNotNull(response);
    }

    @Test
    void register_GraduateWithGmail_Success() {
        graduateRequest.setEmail("graduate@gmail.com");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(graduateRequest);

        assertNotNull(response);
    }

    @Test
    void register_Guest_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(guestRequest);

        assertNotNull(response);
    }

    @Test
    void register_Student_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(studentRequest);

        assertNotNull(response);
    }

    @Test
    void login_ActiveUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }
}