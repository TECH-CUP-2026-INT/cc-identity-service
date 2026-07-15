package co.edu.escuelaing.techcup.identity.domain.port.out;

import java.util.Map;

public interface GoogleOAuthPort {

    /**
     * Validates a Google OAuth token and returns user info (email, name, etc.).
     */
    Map<String, String> validateGoogleToken(String token);
}
