package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class OtpTokenTest {

    @Test
    void validTokenIsNotExpiredAndAcceptsMatchingCode() {
        OtpToken token = TestFixtures.validOtp();

        assertThat(token.isExpired()).isFalse();
        assertThat(token.isValid(TestFixtures.OTP_CODE)).isTrue();
    }

    @Test
    void expiredTokenIsInvalidEvenWithMatchingCode() {
        OtpToken token = TestFixtures.expiredOtp();

        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid(TestFixtures.OTP_CODE)).isFalse();
    }

    @Test
    void usedTokenIsInvalidAndWrongCodeIsRejected() {
        OtpToken token = TestFixtures.validOtp();

        token.markAsUsed();

        assertThat(token.isUsed()).isTrue();
        assertThat(token.isValid(TestFixtures.OTP_CODE)).isFalse();
        assertThat(token.isValid("000000")).isFalse();
    }

    @Test
    void incrementFailedAttemptsUpdatesCounter() {
        OtpToken token = TestFixtures.validOtp();

        token.incrementFailedAttempts();
        token.incrementFailedAttempts();

        assertThat(token.getFailedAttempts()).isEqualTo(2);
    }

    @Test
    void tokenExpiringNowBecomesExpiredAfterTimePasses() {
        OtpToken token = TestFixtures.validOtp();
        token.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).minusNanos(1));

        assertThat(token.isExpired()).isTrue();
    }
}
