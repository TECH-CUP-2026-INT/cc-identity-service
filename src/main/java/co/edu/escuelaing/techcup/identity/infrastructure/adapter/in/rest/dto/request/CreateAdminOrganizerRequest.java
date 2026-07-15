package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Solicitud de creación de cuenta de administrador u organizador de la plataforma TechCup")
public class CreateAdminOrganizerRequest {

    @NotBlank(message = "Full name is required")
    @Schema(description = "Nombre completo del administrador u organizador", example = "María García López")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Correo institucional (@escuelaing.edu.co)", example = "maria.garcia@escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Contraseña para la cuenta", example = "AdminPass123!")
    private String password;

    @NotNull(message = "User type is required (ADMIN or ORGANIZER)")
    @Schema(description = "Tipo de usuario. Solo se permiten ADMIN u ORGANIZER en este endpoint.", example = "ADMIN", allowableValues = {"ADMIN", "ORGANIZER"})
    private UserType userType;
}
