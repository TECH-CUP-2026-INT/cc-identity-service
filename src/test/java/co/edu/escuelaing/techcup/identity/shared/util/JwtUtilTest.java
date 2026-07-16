package co.edu.escuelaing.techcup.identity.shared.util;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "defaultSecretKeyForDevelopmentOnlyChangeInProduction2026!";

    @Test
    void generateTokenStoresSubjectEmailAndRoleClaims() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3_600_000L);

        String token = jwtUtil.generateToken(TestFixtures.USER_ID, "student@escuelaing.edu.co", UserRole.PLAYER);

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(TestFixtures.USER_ID);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("student@escuelaing.edu.co");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("PLAYER");
    }

    @Test
    void invalidTokenIsReportedAsInvalidAndCannotExtractClaims() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 3_600_000L);

        assertThat(jwtUtil.isTokenValid("not-a-jwt")).isFalse();
        assertThatThrownBy(() -> jwtUtil.extractClaims("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void expiredTokenIsReportedAsInvalid() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -1_000L);

        String expiredToken = jwtUtil.generateToken(TestFixtures.USER_ID, "student@escuelaing.edu.co", UserRole.PLAYER);

        assertThat(jwtUtil.isTokenValid(expiredToken)).isFalse();
    }
}
