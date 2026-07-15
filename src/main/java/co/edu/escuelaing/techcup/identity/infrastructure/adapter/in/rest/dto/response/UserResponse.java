package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.IdType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos del usuario registrado en la plataforma TechCup. Los campos opcionales dependen del tipo de usuario.")
public class UserResponse {

    @Schema(description = "ID único del usuario en MongoDB", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String id;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez García")
    private String fullName;

    @Schema(description = "Correo institucional del usuario", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @Schema(description = "Tipo de usuario según su relación con la institución", example = "STUDENT")
    private UserType userType;

    @Schema(description = "Rol del usuario en la plataforma TechCup", example = "PLAYER")
    private UserRole role;

    @Schema(description = "Estado de la cuenta (ACTIVE, INACTIVE, BLOCKED)", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Tipo de documento de identidad (solo estudiantes/egresados)", example = "CC")
    private IdType idType;

    @Schema(description = "Número de documento de identidad", example = "1234567890")
    private String idNumber;

    @Schema(description = "Fecha de nacimiento del usuario", example = "2000-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Programa académico actual (solo estudiantes)", example = "Ingeniería de Sistemas")
    private String academicProgram;

    @Schema(description = "Semestre actual (solo estudiantes)", example = "7")
    private Integer semester;

    @Schema(description = "ID del estudiante asociado (solo invitados/guests)", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String associatedStudentId;

    @Schema(description = "Relación con el estudiante asociado (solo invitados)", example = "Hermano")
    private String relationship;

    @Schema(description = "Programa académico del que se graduó (solo egresados)", example = "Ingeniería Civil")
    private String formerAcademicProgram;

    @Schema(description = "Fecha y hora de creación de la cuenta", example = "2026-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora de la última actualización", example = "2026-03-20T14:45:00")
    private LocalDateTime updatedAt;
}
