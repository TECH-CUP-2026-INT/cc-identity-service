package co.edu.escuelaing.techcup.identity.domain.port.in;

public interface OtpUseCase {

    /**
     * TC-10: Validate OTP and return JWT token on success.
     */
    String validateOtp(String userId, String otpCode);

    /**
     * TC-10: Resend OTP to user email.
     */
    void resendOtp(String userId);
}
