package co.edu.escuelaing.techcup.identity.domain.port.in;

public interface AuthenticationUseCase {

    /**
     * TC-06: Login with institutional email. Returns a pending OTP session ID.
     */
    String loginWithInstitutionalEmail(String email, String password);

    /**
     * TC-07: Login with Gmail OAuth2. Returns a pending OTP session ID.
     */
    String loginWithGmail(String googleToken);
}
