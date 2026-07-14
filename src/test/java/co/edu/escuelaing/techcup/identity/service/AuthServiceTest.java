package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.document.UserDocument;
import co.edu.escuelaing.techcup.identity.dto.*;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private AuditService auditService;

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
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(UserDocument.class))).thenReturn(user);

        authService.register(request);

        verify(userRepository).save(any(UserDocument.class));
        verify(otpService).generateAndSend(any(UserDocument.class));
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
}