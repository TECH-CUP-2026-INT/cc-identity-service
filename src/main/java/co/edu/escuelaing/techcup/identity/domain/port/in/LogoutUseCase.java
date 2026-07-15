package co.edu.escuelaing.techcup.identity.domain.port.in;

/**
 * TC-29: User Logout - revokes JWT token.
 */
public interface LogoutUseCase {

    void logout(String token);
}
