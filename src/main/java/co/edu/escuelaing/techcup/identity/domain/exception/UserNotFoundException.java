package co.edu.escuelaing.techcup.identity.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String identifier) {
        super("USER_NOT_FOUND", "User not found: " + identifier);
    }
}
