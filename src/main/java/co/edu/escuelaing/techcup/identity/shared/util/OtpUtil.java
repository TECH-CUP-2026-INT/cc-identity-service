package co.edu.escuelaing.techcup.identity.shared.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpUtil {

    private final int otpLength;
    private final String formatPattern;
    private final SecureRandom random = new SecureRandom();

    public OtpUtil(@Value("${otp.length:6}") int otpLength) {
        this.otpLength = otpLength;
        this.formatPattern = String.format("%%0%dd", otpLength);
    }

    public String generateOtp() {
        int bound = (int) Math.pow(10, otpLength);
        int otp = random.nextInt(bound);
        return String.format(formatPattern, otp);
    }

    public String generateRecoveryCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public String generateTemporaryPassword() {
        return java.util.UUID.randomUUID().toString().substring(0, 12);
    }
}
