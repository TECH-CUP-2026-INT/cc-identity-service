package co.edu.escuelaing.techcup.identity.domain.exception;

public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }
}
