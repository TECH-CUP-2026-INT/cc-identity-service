package co.edu.escuelaing.techcup.identity.exception;

import co.edu.escuelaing.techcup.identity.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SCRUM-13: Pruebas unitarias del manejador global de excepciones.
 * Verifica que cada tipo de excepcion retorne el codigo HTTP correcto.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBadCredentials_returns401() {
        BadCredentialsException ex = new BadCredentialsException("bad creds");
        ResponseEntity<ApiResponse> response = handler.handleBadCredentials(ex);
        assertEquals(401, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleDisabled_returns403() {
        DisabledException ex = new DisabledException("disabled");
        ResponseEntity<ApiResponse> response = handler.handleDisabled(ex);
        assertEquals(403, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleRuntime_returns400WithMessage() {
        RuntimeException ex = new RuntimeException("algo salio mal");
        ResponseEntity<ApiResponse> response = handler.handleRuntime(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("algo salio mal", response.getBody().getMessage());
    }

    @Test
    void handleRuntime_withBusinessException_returns400() {
        BusinessException ex = new BusinessException("User not found");
        ResponseEntity<ApiResponse> response = handler.handleRuntime(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        FieldError fieldError = new FieldError("obj", "email", "must not be blank");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ApiResponse> response = handler.handleValidation(ex);
        assertEquals(400, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("email"));
    }
}
