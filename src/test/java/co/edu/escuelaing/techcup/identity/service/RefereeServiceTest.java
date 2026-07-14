package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.User;
import co.edu.escuelaing.techcup.identity.entity.UserRole;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
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
class RefereeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefereeService refereeService;

    private RefereeRequestDTO request;

    @BeforeEach
    void setUp() {
        request = new RefereeRequestDTO();
        request.setEmail("referee@email.com");
        request.setName("Referee User");
    }

    @Test
    void createReferee_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = refereeService.createReferee(request);

        assertNotNull(result);
        assertTrue(result.contains("Árbitro creado exitosamente"));
        assertTrue(result.contains("Contraseña temporal:"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createReferee_EmailAlreadyExists_ThrowsException() {
        User existingUser = User.builder()
                .email("referee@email.com")
                .role(UserRole.REFEREE)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        assertThrows(BusinessException.class, () -> refereeService.createReferee(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createReferee_UserSavedWithCorrectRole() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id("ref-123")
                .email(request.getEmail())
                .name(request.getName())
                .role(UserRole.REFEREE)
                .isActive(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String result = refereeService.createReferee(request);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }
}