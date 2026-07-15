package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
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
@Schema(description = "DTO interno para creación de credenciales desde users-players-service. " +
        "Usado durante el registro de estudiantes (TC-01), invitados (TC-02) y egresados (TC-03).")
public class CreateCredentialRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Correo institucional del usuario", example = "estudiante@escuelaing.edu.co")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Contraseña en texto plano (se almacenará con hash BCrypt)", example = "Password123!")
    private String password;

    @NotBlank(message = "Full name is required")
    @Schema(description = "Nombre completo del usuario", example = "Ada Lovelace")
    private String fullName;

    @NotNull(message = "User type is required")
    @Schema(description = "Tipo de usuario según su relación con la institución", example = "STUDENT", allowableValues = {"STUDENT", "GUEST", "GRADUATE"})
    private UserType userType;

    @NotNull(message = "Role is required")
    @Schema(description = "Rol del usuario en la plataforma TechCup", example = "PLAYER", allowableValues = {"PLAYER", "VIEWER"})
    private UserRole role;
}
