package co.edu.escuelaing.techcup.identity.domain.exception;

public class InvalidOtpException extends DomainException {

    public InvalidOtpException(String message) {
        super("INVALID_OTP", message);
    }
}
