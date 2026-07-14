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
class AuthServiceFullTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@universidad.edu.co");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setRole(UserRole.STUDENT);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@universidad.edu.co");
        loginRequest.setPassword("password123");

        user = User.builder()
                .id("123")
                .email("test@universidad.edu.co")
                .password("encoded")
                .name("Test User")
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();
    }

    @Test
    void register_Student_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Admin_Success() {
        registerRequest.setRole(UserRole.ADMIN);
        registerRequest.setEmail("admin@techcup.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Organizer_Success() {
        registerRequest.setRole(UserRole.ORGANIZER);
        registerRequest.setEmail("organizer@techcup.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Graduate_UniversityEmail_Success() {
        registerRequest.setRole(UserRole.GRADUATE);
        registerRequest.setEmail("graduate@universidad.edu.co");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Graduate_Gmail_Success() {
        registerRequest.setRole(UserRole.GRADUATE);
        registerRequest.setEmail("graduate@gmail.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_Guest_Success() {
        registerRequest.setRole(UserRole.GUEST);
        registerRequest.setEmail("guest@gmail.com");
        registerRequest.setAssociatedStudentId("student-123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_StudentInvalidEmail_ThrowsException() {
        registerRequest.setEmail("student@gmail.com");

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_GuestNoStudentId_ThrowsException() {
        registerRequest.setRole(UserRole.GUEST);
        registerRequest.setEmail("guest@gmail.com");
        registerRequest.setAssociatedStudentId(null);

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_GuestInvalidEmail_ThrowsException() {
        registerRequest.setRole(UserRole.GUEST);
        registerRequest.setEmail("guest@yahoo.com");
        registerRequest.setAssociatedStudentId("student-123");

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_GraduateInvalidEmail_ThrowsException() {
        registerRequest.setRole(UserRole.GRADUATE);
        registerRequest.setEmail("graduate@yahoo.com");

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_RoleNull_ThrowsException() {
        registerRequest.setRole(null);

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ValidCredentials_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserInactive_ThrowsException() {
        user.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }
}