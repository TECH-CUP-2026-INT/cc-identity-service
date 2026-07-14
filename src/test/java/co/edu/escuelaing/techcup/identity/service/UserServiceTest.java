package co.edu.escuelaing.techcup.identity.service;

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
    private AuditService auditService;
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
        auditService = mock(AuditService.class);

        userService = new UserService(
                userRepository, passwordEncoder, otpService, emailService,
                refereeValidator, passwordGenerator, nameSplitter, auditService
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
}