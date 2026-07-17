package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Google OAuth 2.0 login request")
public class GoogleLoginRequest {

    @NotBlank(message = "Google token is required")
    @Schema(description = "ID token obtained from the Google OAuth 2.0 consent flow", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String googleToken;
}
