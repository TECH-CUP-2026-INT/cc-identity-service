package co.edu.escuelaing.techcup.identity.exception;

import co.edu.escuelaing.techcup.identity.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
<<<<<<< HEAD
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
=======
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

>>>>>>> origin/develop
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SCRUM-13: Pruebas unitarias del manejador global de excepciones.
 * Verifica que cada tipo de excepcion retorne el codigo HTTP correcto.
=======
/**
 * SCRUM-61: Inhabilitar usuario.
 * Pruebas unitarias del manejador global de excepciones.
 * Verifica que cada tipo de excepción retorna el código HTTP
 * y el mensaje correcto sin depender del contexto de Spring.
>>>>>>> origin/develop
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

<<<<<<< HEAD
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
=======
    /**
     * Verifica que BadCredentialsException retorna 401 con mensaje de credenciales inválidas.
     */
    @Test
    void handleBadCredentials_returns401() {
        BadCredentialsException ex = new BadCredentialsException("bad credentials");

        ResponseEntity<ApiResponse> response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid email or password.", response.getBody().getMessage());
    }

    /**
     * Verifica que DisabledException retorna 403 con mensaje de cuenta no verificada.
     */
    @Test
    void handleDisabled_returns403() {
        DisabledException ex = new DisabledException("disabled");

        ResponseEntity<ApiResponse> response = handler.handleDisabled(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Account not verified. Check your email for the OTP.", response.getBody().getMessage());
    }

    /**
     * Verifica que RuntimeException retorna 400 con el mensaje de la excepción.
     */
    @Test
    void handleRuntime_returns400WithMessage() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<ApiResponse> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Something went wrong", response.getBody().getMessage());
    }

    /**
     * Verifica que BusinessException (subclase de RuntimeException) retorna 400.
     */
    @Test
    void handleRuntime_withBusinessException_returns400() {
        BusinessException ex = new BusinessException("User not found");

        ResponseEntity<ApiResponse> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }



    /**
 * Verifica que MethodArgumentNotValidException retorna 400 con los campos invalidos.
 */
@Test
void handleValidation_returns400WithFieldErrors() {
    FieldError fieldError = new FieldError("request", "email", "Email is required");
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<ApiResponse> response = handler.handleValidation(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertFalse(response.getBody().isSuccess());
    assertTrue(response.getBody().getMessage().contains("email"));
    assertTrue(response.getBody().getMessage().contains("Email is required"));
}
}
>>>>>>> origin/develop
