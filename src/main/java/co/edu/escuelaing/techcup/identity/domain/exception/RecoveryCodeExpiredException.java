package co.edu.escuelaing.techcup.identity.domain.exception;

public class RecoveryCodeExpiredException extends DomainException {

    public RecoveryCodeExpiredException() {
        super("RECOVERY_CODE_EXPIRED", "The recovery code has expired. Please request a new one.");
    }
}
