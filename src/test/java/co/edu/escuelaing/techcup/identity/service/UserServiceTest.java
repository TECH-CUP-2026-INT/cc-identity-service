package co.edu.escuelaing.techcup.identity.service;
import java.util.Optional;
import java.util.UUID;
import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.IdType;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OtpService otpService;
    private EmailService emailService;
    private RefereeValidator refereeValidator;
    private TemporaryPasswordGenerator passwordGenerator;
    private FullNameSplitter nameSplitter;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        otpService = mock(OtpService.class);
        emailService = mock(EmailService.class);
        refereeValidator = mock(RefereeValidator.class);
        passwordGenerator = mock(TemporaryPasswordGenerator.class);
        nameSplitter = mock(FullNameSplitter.class);

        userService = new UserService(
                userRepository, passwordEncoder, otpService, emailService,
                refereeValidator, passwordGenerator, nameSplitter
        );
    }

    @Test
    void createsRefereeWithCorrectRoleAndStatus() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(2000, 1, 1), IdType.CC, "12345678", "juan@gmail.com"
        );

        when(passwordGenerator.generate()).thenReturn("temp1234");
        when(nameSplitter.split("Juan Perez")).thenReturn(new String[]{"Juan", "Perez"});
        when(passwordEncoder.encode("temp1234")).thenReturn("hashedPassword");

        userService.createReferee(dto);

        verify(refereeValidator).validate(dto);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity saved = captor.getValue();
        assertEquals("Juan", saved.getFirstName());
        assertEquals("Perez", saved.getLastName());
        assertEquals("juan@gmail.com", saved.getEmail());
        assertEquals(UserEntity.Role.REFEREE, saved.getRole());
        assertTrue(saved.isEnabled());
        assertEquals("hashedPassword", saved.getPassword());
        assertEquals(IdType.CC, saved.getIdType());
        assertEquals("12345678", saved.getIdNumber());
    }

    @Test
    void triggersEmailAndOtpAfterSaving() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Ana Gomez", LocalDate.of(1995, 5, 20), IdType.CC, "87654321", "ana@gmail.com"
        );

        when(passwordGenerator.generate()).thenReturn("temp5678");
        when(nameSplitter.split("Ana Gomez")).thenReturn(new String[]{"Ana", "Gomez"});
        when(passwordEncoder.encode("temp5678")).thenReturn("hashedPass2");

        userService.createReferee(dto);

        verify(emailService).sendRefereeCredentials(eq("ana@gmail.com"), eq("temp5678"));
        verify(otpService).generateAndSend(any(UserEntity.class));
    }

    @Test
    void doesNotSaveIfValidationFails() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Carlos Ruiz", LocalDate.of(1998, 3, 10), IdType.CC, "11223344", "carlos@hotmail.com"
        );

        doThrow(new BusinessException("Gmail requerido"))
                .when(refereeValidator).validate(dto);

        assertThrows(BusinessException.class, () -> userService.createReferee(dto));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendRefereeCredentials(anyString(), anyString());
        verify(otpService, never()).generateAndSend(any());
    }

    /**
     * SCRUM-61: Inhabilitar usuario.
     * Verifica que un usuario activo puede ser inhabilitado correctamente.
     */
    @Test
    void disableUser_success() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("player@test.com")
                .role(UserEntity.Role.USER)
                .enabled(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.disableUser(userId);

        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }

    /**
     * SCRUM-61: Inhabilitar usuario.
     * Verifica que se lanza excepción si el usuario no existe.
     */
    @Test
    void disableUser_userNotFound_throwsBusinessException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.disableUser(userId));

        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    /**
     * SCRUM-61: Inhabilitar usuario.
     * Verifica que no se puede inhabilitar una cuenta con rol ADMIN.
     */
    @Test
    void disableUser_adminAccount_throwsBusinessException() {
        UUID userId = UUID.randomUUID();
        UserEntity admin = UserEntity.builder()
                .id(userId)
                .email("admin@test.com")
                .role(UserEntity.Role.ADMIN)
                .enabled(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.disableUser(userId));

        assertEquals("Admin accounts cannot be disabled", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    /**
     * TESTS PARA: 
     * SCRUM-61: Inhabilitar usuario.
     * Verifica que no se puede inhabilitar un usuario que ya está deshabilitado.
     */
    @Test
    void disableUser_alreadyDisabled_throwsBusinessException() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("player@test.com")
                .role(UserEntity.Role.USER)
                .enabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.disableUser(userId));

        assertEquals("User is already disabled", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}