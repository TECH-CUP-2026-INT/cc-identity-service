package co.edu.escuelaing.techcup.identity.domain.exception;

public class UserProfileUnavailableException extends DomainException {

    public UserProfileUnavailableException(String userId) {
        super("USER_PROFILE_UNAVAILABLE",
                "Could not verify current role/status for user " + userId + " with users-players-service");
    }
}
