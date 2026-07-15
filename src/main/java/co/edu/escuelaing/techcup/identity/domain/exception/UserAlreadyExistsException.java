package co.edu.escuelaing.techcup.identity.domain.exception;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "A user with email " + email + " already exists");
    }
}
