package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.handler;

import co.edu.escuelaing.techcup.identity.domain.exception.AccountBlockedException;
import co.edu.escuelaing.techcup.identity.domain.exception.AccountInactiveException;
import co.edu.escuelaing.techcup.identity.domain.exception.DomainException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidCredentialsException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidEmailDomainException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidOtpException;
import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.exception.RecoveryCodeExpiredException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsKnownDomainExceptionsToExpectedHttpStatuses() {
        assertError(handler.handleUserAlreadyExists(new UserAlreadyExistsException("user@escuelaing.edu.co")),
                HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
        assertError(handler.handleUserNotFound(new UserNotFoundException("missing")),
                HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        assertError(handler.handleInvalidCredentials(new InvalidCredentialsException()),
                HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        assertError(handler.handleAccountInactive(new AccountInactiveException()),
                HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE");
        assertError(handler.handleAccountBlocked(new AccountBlockedException()),
                HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED");
        assertError(handler.handleInvalidOtp(new InvalidOtpException("bad otp")),
                HttpStatus.BAD_REQUEST, "INVALID_OTP");
        assertError(handler.handleInvalidToken(new InvalidTokenException("bad token")),
                HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        assertError(handler.handleInvalidEmailDomain(new InvalidEmailDomainException("gmail.com")),
                HttpStatus.BAD_REQUEST, "INVALID_EMAIL_DOMAIN");
        assertError(handler.handleRecoveryCodeExpired(new RecoveryCodeExpiredException()),
                HttpStatus.GONE, "RECOVERY_CODE_EXPIRED");
        assertError(handler.handleDomainException(new DomainException("DOMAIN", "domain error")),
                HttpStatus.BAD_REQUEST, "DOMAIN");
    }

    @Test
    void mapsUnexpectedExceptionToInternalServerError() {
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(new RuntimeException("boom"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }

    private void assertError(ResponseEntity<Map<String, Object>> response, HttpStatus status, String errorCode) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("errorCode", errorCode);
        assertThat(response.getBody()).containsKeys("timestamp", "message");
    }
}
