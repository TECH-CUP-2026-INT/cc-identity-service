package co.edu.escuelaing.techcup.identity.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder encoder = new PasswordEncoder();

    @Test
    void encode_ValidPassword_ReturnsEncoded() {
        String password = "password123";
        String encoded = encoder.encode(password);
        
        assertNotNull(encoded);
        assertNotEquals(password, encoded);
        assertTrue(encoded.length() > 0);
    }

    @Test
    void encode_EmptyPassword_ReturnsEncoded() {
        String encoded = encoder.encode("");
        
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
    }

    @Test
    void matches_ValidPassword_ReturnsTrue() {
        String password = "password123";
        String encoded = encoder.encode(password);
        
        assertTrue(encoder.matches(password, encoded));
    }

    @Test
    void matches_InvalidPassword_ReturnsFalse() {
        String password = "password123";
        String encoded = encoder.encode(password);
        
        assertFalse(encoder.matches("wrongpassword", encoded));
    }

    @Test
    void matches_EmptyPassword_ReturnsTrue() {
        String encoded = encoder.encode("");
        assertTrue(encoder.matches("", encoded));
    }

    @Test
    void matches_EmptyPasswordWithWrongInput_ReturnsFalse() {
        String encoded = encoder.encode("");
        assertFalse(encoder.matches("wrong", encoded));
    }
}