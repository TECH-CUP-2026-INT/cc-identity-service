package co.edu.escuelaing.techcup.identity.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordUtilTest {

    private final PasswordUtil passwordUtil = new PasswordUtil();

    @Test
    void encodeHashesPasswordAndMatchesRawPassword() {
        String encoded = passwordUtil.encode("Password123!");

        assertThat(encoded).isNotEqualTo("Password123!");
        assertThat(passwordUtil.matches("Password123!", encoded)).isTrue();
    }

    @Test
    void matchesReturnsFalseForDifferentPassword() {
        String encoded = passwordUtil.encode("Password123!");

        assertThat(passwordUtil.matches("OtherPassword123!", encoded)).isFalse();
    }
}
