package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.IdType;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefereeValidatorTest {

    private UserRepository userRepository;
    private RefereeValidator validator;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        validator = new RefereeValidator(userRepository);
    }

    @Test
    void rejectsNonGmailEmail() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(2000, 1, 1), IdType.CC, "12345678", "juan@hotmail.com"
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(dto));
        assertTrue(ex.getMessage().contains("Gmail"));
    }

    @Test
    void rejectsDuplicateEmail() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(2000, 1, 1), IdType.CC, "12345678", "juan@gmail.com"
        );
        when(userRepository.existsByEmail("juan@gmail.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(dto));
        assertTrue(ex.getMessage().contains("correo"));
    }

    @Test
    void rejectsDuplicateIdNumber() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(2000, 1, 1), IdType.CC, "12345678", "juan@gmail.com"
        );
        when(userRepository.existsByEmail("juan@gmail.com")).thenReturn(false);
        when(userRepository.existsByIdNumber("12345678")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(dto));
        assertTrue(ex.getMessage().contains("identificación"));
    }

    @Test
    void acceptsValidReferee() {
        RefereeRequestDTO dto = new RefereeRequestDTO(
                "Juan Perez", LocalDate.of(2000, 1, 1), IdType.CC, "12345678", "juan@gmail.com"
        );
        when(userRepository.existsByEmail("juan@gmail.com")).thenReturn(false);
        when(userRepository.existsByIdNumber("12345678")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validate(dto));
    }
}