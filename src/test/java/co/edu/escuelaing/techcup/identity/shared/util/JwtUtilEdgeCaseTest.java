package co.edu.escuelaing.techcup.identity.shared.util;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilEdgeCaseTest {

    private static final String SECRET = "defaultSecretKeyForDevelopmentOnlyChangeInProduction2026!";

    @Test
    void constructorRejectsSecretThatIsTooShortForHs256() {
        assertThatThrownBy(() -> new JwtUtil("short-secret", 3_600_000L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void isTokenValidReturnsFalseForNullBlankAndTamperedTokenValues() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3_600_000L);
        String token = jwtUtil.generateToken(TestFixtures.USER_ID, "student@escuelaing.edu.co", UserRole.PLAYER);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtUtil.isTokenValid(null)).isFalse();
        assertThat(jwtUtil.isTokenValid("   ")).isFalse();
        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }
}
