package co.edu.escuelaing.techcup.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for OTP verification.
 * Contains the email and the 6-digit code sent to the user (SCRUM-13).
 */
public class OtpVerifyRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String code;

    public OtpVerifyRequest() {}

    public String getEmail() { return email; }
    public String getCode() { return code; }

    public void setEmail(String email) { this.email = email; }
    public void setCode(String code) { this.code = code; }
}