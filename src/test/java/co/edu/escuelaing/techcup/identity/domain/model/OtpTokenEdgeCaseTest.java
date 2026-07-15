package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class OtpTokenEdgeCaseTest {

    @Test
    void isValidRejectsNullInputCodeWithoutThrowing() {
        OtpToken token = TestFixtures.validOtp();

        assertThat(token.isValid(null)).isFalse();
    }

    @Test
    void isValidRejectsCodesWithLeadingOrTrailingSpaces() {
        OtpToken token = TestFixtures.validOtp();

        assertThat(token.isValid(" " + TestFixtures.OTP_CODE)).isFalse();
        assertThat(token.isValid(TestFixtures.OTP_CODE + " ")).isFalse();
    }

    @Test
    void isExpiredReturnsFalseWhenExpirationIsStillInTheFutureByANarrowMargin() {
        OtpToken token = TestFixtures.validOtp();
        token.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusSeconds(1));

        assertThat(token.isExpired()).isFalse();
    }
}
