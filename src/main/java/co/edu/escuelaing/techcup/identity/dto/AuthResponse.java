package co.edu.escuelaing.techcup.identity.dto;

/**
 * Response DTO returned after successful login or token refresh.
 * Contains the access token, refresh token, and basic user info (SCRUM-14, SCRUM-15).
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String role;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String email, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
}