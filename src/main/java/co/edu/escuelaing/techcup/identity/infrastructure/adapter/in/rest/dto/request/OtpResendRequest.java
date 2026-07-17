package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "OTP code resend request")
public class OtpResendRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user to resend the OTP to", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID userId;
}
