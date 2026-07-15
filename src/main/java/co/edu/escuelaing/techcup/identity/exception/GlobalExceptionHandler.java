package co.edu.escuelaing.techcup.identity.exception;

import co.edu.escuelaing.techcup.identity.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.stream.Collectors;

/**
 * Catches exceptions thrown anywhere in the application and returns
 * clean, consistent HTTP responses instead of Spring's default error page.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation failures (missing fields, bad format, etc).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(errors, false));
    }

    /**
     * Handles wrong email or password on login.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse("Invalid email or password.", false));
    }

    /**
     * Handles login attempts on accounts that haven't verified their OTP yet.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse> handleDisabled(DisabledException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse("Account not verified. Check your email for the OTP.", false));
    }

    /**
     * Handles invalid date range and other illegal argument errors.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(ex.getMessage(), false));
    }

    /**
     * Catches any other RuntimeException not handled above.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntime(RuntimeException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(ex.getMessage(), false));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Unauthorized", false));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = auth == null || auth instanceof AnonymousAuthenticationToken;
        if (isAnonymous) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Unauthorized", false));
        }
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse("Access denied", false));
    }
}