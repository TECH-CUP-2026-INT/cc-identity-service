package co.edu.escuelaing.techcup.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an access token.
 * Contains the refresh token issued during login (SCRUM-15).
 */
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public RefreshTokenRequest() {}

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}