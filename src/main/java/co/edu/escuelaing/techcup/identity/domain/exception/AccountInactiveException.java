package co.edu.escuelaing.techcup.identity.domain.exception;

public class AccountInactiveException extends DomainException {

    public AccountInactiveException() {
        super("ACCOUNT_INACTIVE", "The account is inactive");
    }
}
