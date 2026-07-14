package co.edu.escuelaing.techcup.identity.dto;
/**
 * Generic response DTO for simple messages.
 * Used when no data needs to be returned, only a status message (e.g. "OTP sent").
 */
public class ApiResponse {

    private String message;
    private boolean success;

    public ApiResponse() {}

    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }

    public void setMessage(String message) { this.message = message; }
    public void setSuccess(boolean success) { this.success = success; }
}