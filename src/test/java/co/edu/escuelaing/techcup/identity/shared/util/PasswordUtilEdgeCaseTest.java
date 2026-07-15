package co.edu.escuelaing.techcup.identity.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordUtilEdgeCaseTest {

    private final PasswordUtil passwordUtil = new PasswordUtil();

    @Test
    void emptyPasswordCanBeEncodedButOnlyMatchesEmptyRawPassword() {
        String encoded = passwordUtil.encode("");

        assertThat(passwordUtil.matches("", encoded)).isTrue();
        assertThat(passwordUtil.matches(" ", encoded)).isFalse();
    }

    @Test
    void nullPasswordInputsAreRejectedByEncoderOrMatcher() {
        assertThatThrownBy(() -> passwordUtil.encode(null))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> passwordUtil.matches(null, passwordUtil.encode("Password123!")))
                .isInstanceOf(RuntimeException.class);
    }
}
