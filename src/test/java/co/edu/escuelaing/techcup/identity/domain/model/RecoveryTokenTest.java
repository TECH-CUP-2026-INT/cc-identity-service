package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecoveryTokenTest {

    @Test
    void validRecoveryTokenAcceptsMatchingCode() {
        RecoveryToken token = TestFixtures.validRecoveryToken();

        assertThat(token.isExpired()).isFalse();
        assertThat(token.isValid("ABCD1234")).isTrue();
    }

    @Test
    void expiredRecoveryTokenIsInvalid() {
        RecoveryToken token = TestFixtures.expiredRecoveryToken();

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid("ABCD1234")).isFalse();
    }

    @Test
    void usedRecoveryTokenIsInvalid() {
        RecoveryToken token = TestFixtures.validRecoveryToken();

        token.markAsUsed();

        assertThat(token.isUsed()).isTrue();
        assertThat(token.isValid("ABCD1234")).isFalse();
        assertThat(token.isValid("WRONG999")).isFalse();
    }
}
