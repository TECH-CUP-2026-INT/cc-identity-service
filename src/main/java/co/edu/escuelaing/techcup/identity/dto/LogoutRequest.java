package co.edu.escuelaing.techcup.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for logging out.
 * Contains the refresh token to invalidate (SCRUM-18).
 */
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public LogoutRequest() {}

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
