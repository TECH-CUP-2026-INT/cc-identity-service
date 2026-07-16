package co.edu.escuelaing.techcup.identity.domain.port.in;

import java.util.UUID;

public interface OtpUseCase {

    /**
     * TC-10: Validate OTP and return JWT token on success.
     */
    String validateOtp(UUID userId, String otpCode);

    /**
     * TC-10: Resend OTP to user email.
     */
    void resendOtp(UUID userId);
}
