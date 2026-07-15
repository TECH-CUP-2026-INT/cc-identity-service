package co.edu.escuelaing.techcup.identity.domain.port.in;

public interface PasswordRecoveryUseCase {

    /**
     * TC-09: Request password recovery. Sends code to email.
     * Does NOT reveal whether the email exists (security measure).
     */
    void requestRecovery(String email);

    /**
     * TC-09: Reset password using recovery code.
     */
    void resetPassword(String email, String recoveryCode, String newPassword);
}
