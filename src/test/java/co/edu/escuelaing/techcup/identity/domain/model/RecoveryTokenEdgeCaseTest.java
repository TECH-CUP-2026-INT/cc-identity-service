package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveryTokenEdgeCaseTest {

    @Test
    void isValidRejectsNullInputCodeWithoutThrowing() {
        RecoveryToken token = TestFixtures.validRecoveryToken();

        assertThat(token.isValid(null)).isFalse();
    }

    @Test
    void isValidRejectsRecoveryCodeWithDifferentCasingOrSpaces() {
        RecoveryToken token = TestFixtures.validRecoveryToken();

        assertThat(token.isValid("abcd1234")).isFalse();
        assertThat(token.isValid(" ABCD1234 ")).isFalse();
    }

    @Test
    void isExpiredReturnsFalseWhenExpirationIsStillInTheFutureByANarrowMargin() {
        RecoveryToken token = TestFixtures.validRecoveryToken();
        token.setExpiresAt(LocalDateTime.now().plusSeconds(1));

        assertThat(token.isExpired()).isFalse();
    }
}
