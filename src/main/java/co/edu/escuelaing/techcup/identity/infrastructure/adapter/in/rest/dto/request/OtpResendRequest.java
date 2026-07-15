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
@Schema(description = "Solicitud de reenvío de código OTP")
public class OtpResendRequest {

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID del usuario al que se le reenviará el OTP", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String userId;
}
