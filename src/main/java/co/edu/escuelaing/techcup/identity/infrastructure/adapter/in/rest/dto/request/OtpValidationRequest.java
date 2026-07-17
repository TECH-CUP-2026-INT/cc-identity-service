package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP code verification request to complete the two-factor authentication flow")
public class OtpValidationRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user who received the OTP during login", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;

    @NotBlank(message = "OTP code is required")
    @Schema(description = "6-digit OTP code sent to the user email", example = "482917")
    private String otpCode;
}
