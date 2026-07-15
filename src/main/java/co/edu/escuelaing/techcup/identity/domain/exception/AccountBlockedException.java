package co.edu.escuelaing.techcup.identity.domain.exception;

public class AccountBlockedException extends DomainException {

    public AccountBlockedException() {
        super("ACCOUNT_BLOCKED", "Account has been blocked due to multiple failed OTP attempts");
    }
}
