package co.edu.escuelaing.techcup.identity.domain.exception;

public class InvalidEmailDomainException extends DomainException {

    public InvalidEmailDomainException(String expected) {
        super("INVALID_EMAIL_DOMAIN", "Email must belong to domain: " + expected);
    }
}
