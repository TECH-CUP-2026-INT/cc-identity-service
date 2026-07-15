package co.edu.escuelaing.techcup.identity.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtpUtilTest {

    @Test
    void generateOtpUsesConfiguredLengthAndZeroPadding() {
        OtpUtil otpUtil = new OtpUtil(6);

        String otp = otpUtil.generateOtp();

        assertThat(otp).matches("\\d{6}");
    }

    @Test
    void generateRecoveryCodeProducesUppercaseEightCharacterCode() {
        OtpUtil otpUtil = new OtpUtil(6);

        String code = otpUtil.generateRecoveryCode();

        assertThat(code).hasSize(8).matches("[A-F0-9]{8}");
    }

    @Test
    void generateTemporaryPasswordProducesTwelveCharacters() {
        OtpUtil otpUtil = new OtpUtil(6);

        String password = otpUtil.generateTemporaryPassword();

        assertThat(password).hasSize(12);
    }
}
