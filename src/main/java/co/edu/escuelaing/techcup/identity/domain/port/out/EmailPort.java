package co.edu.escuelaing.techcup.identity.domain.port.out;

public interface EmailPort {

    void sendOtp(String toEmail, String otpCode);

    void sendRecoveryCode(String toEmail, String recoveryCode);

    void sendTemporaryCredentials(String toEmail, String temporaryPassword);
}
