package co.edu.escuelaing.techcup.identity.util;

import co.edu.escuelaing.techcup.identity.entity.User;
import co.edu.escuelaing.techcup.identity.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private User user;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        user = User.builder()
                .id("123")
                .email("test@email.com")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    void generateAccessToken_ReturnsToken() {
        String token = provider.generateAccessToken(user);
        
        assertNotNull(token);
        assertTrue(token.startsWith("access-token-"));
        assertTrue(token.contains(user.getId()));
    }

    @Test
    void generateRefreshToken_ReturnsToken() {
        String token = provider.generateRefreshToken(user);
        
        assertNotNull(token);
        assertTrue(token.startsWith("refresh-token-"));
        assertTrue(token.contains(user.getId()));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = provider.generateAccessToken(user);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertFalse(provider.validateToken("invalid-token"));
        assertFalse(provider.validateToken(null));
        assertFalse(provider.validateToken(""));
    }

    @Test
    void getUserIdFromToken_ValidToken_ReturnsUserId() {
        String token = provider.generateAccessToken(user);
        String userId = provider.getUserIdFromToken(token);
        
        assertEquals(user.getId(), userId);
    }

    @Test
    void getUserIdFromToken_RefreshToken_ReturnsNull() {
        String token = provider.generateRefreshToken(user);
        String userId = provider.getUserIdFromToken(token);
        
        assertNull(userId);
    }

    @Test
    void getUserIdFromToken_InvalidToken_ReturnsNull() {
        assertNull(provider.getUserIdFromToken("invalid-token"));
        assertNull(provider.getUserIdFromToken(null));
        assertNull(provider.getUserIdFromToken(""));
    }

    @Test
    void getUserIdFromToken_TokenWithoutUserId_ReturnsNull() {
        String token = "access-token-";
        assertNull(provider.getUserIdFromToken(token));
    }
}