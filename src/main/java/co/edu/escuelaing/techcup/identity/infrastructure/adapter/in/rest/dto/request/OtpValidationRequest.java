package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

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
public class OtpValidationRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "OTP code is required")
    private String otpCode;
}
