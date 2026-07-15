package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.oauth;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidCredentialsException;
import co.edu.escuelaing.techcup.identity.domain.port.out.GoogleOAuthPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GoogleOAuthAdapter implements GoogleOAuthPort {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> validateGoogleToken(String googleToken) {
        log.info("Validating Google OAuth token");
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(
                    GOOGLE_TOKEN_INFO_URL + googleToken, Map.class);

            if (response == null || !googleClientId.equals(response.get("aud"))) {
                throw new InvalidCredentialsException("Invalid Google token: audience mismatch");
            }

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("email", (String) response.get("email"));
            userInfo.put("name", (String) response.get("name"));
            userInfo.put("sub", (String) response.get("sub"));
            return userInfo;

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating Google token: {}", e.getMessage());
            throw new InvalidCredentialsException("Failed to validate Google token");
        }
    }
}
