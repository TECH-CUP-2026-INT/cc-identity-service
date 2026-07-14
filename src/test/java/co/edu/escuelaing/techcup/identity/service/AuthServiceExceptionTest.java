package co.edu.escuelaing.techcup.identity.service;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceExceptionTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@universidad.edu.co");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setRole(UserRole.STUDENT);
    }

    @Test
    void register_GuestWithoutStudentId_ThrowsException() {
        registerRequest.setRole(UserRole.GUEST);
        registerRequest.setEmail("guest@gmail.com");
        registerRequest.setAssociatedStudentId(null);

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_GraduateWithInvalidEmail_ThrowsException() {
        registerRequest.setRole(UserRole.GRADUATE);
        registerRequest.setEmail("graduate@yahoo.com");

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_StudentWithInvalidEmail_ThrowsException() {
        registerRequest.setEmail("student@gmail.com");

        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
    }
}