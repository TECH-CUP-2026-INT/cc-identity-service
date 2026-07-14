package co.edu.escuelaing.techcup.identity.util;

import co.edu.escuelaing.techcup.identity.entity.User;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    public String generateAccessToken(User user) {
        // SIMPLIFICADO: En producción usar JWT real con librería
        return "access-token-" + user.getId() + "-" + UUID.randomUUID();
    }

    public String generateRefreshToken(User user) {
        return "refresh-token-" + user.getId() + "-" + UUID.randomUUID();
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("access-token-");
    }

    public String getUserIdFromToken(String token) {
        if (token != null && token.startsWith("access-token-")) {
            String[] parts = token.split("-");
            if (parts.length >= 3) {
                return parts[2];
            }
        }
        return null;
    }
}