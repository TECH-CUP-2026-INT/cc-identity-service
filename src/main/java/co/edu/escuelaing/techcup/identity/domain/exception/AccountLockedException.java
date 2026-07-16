package co.edu.escuelaing.techcup.identity.domain.exception;

import java.time.LocalDateTime;

public class AccountLockedException extends DomainException {

    public AccountLockedException(LocalDateTime lockedUntil) {
        super("ACCOUNT_LOCKED", "Account is locked due to multiple failed login attempts. Try again after " + lockedUntil);
    }
}
