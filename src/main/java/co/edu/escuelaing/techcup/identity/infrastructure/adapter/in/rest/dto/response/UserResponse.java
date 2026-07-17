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
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User data registered in the TechCup platform. Optional fields depend on user type.")
public class UserResponse {

    @Schema(description = "Unique user ID (source of truth: users-players-service)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "User full name", example = "Juan Pérez García")
    private String fullName;

    @Schema(description = "User institutional email", example = "juan.perez@escuelaing.edu.co")
    private String email;

    @Schema(description = "User type based on their relationship with the institution", example = "STUDENT")
    private UserType userType;

    @Schema(description = "User role in the TechCup platform", example = "PLAYER")
    private UserRole role;

    @Schema(description = "Account status (ACTIVE, INACTIVE, BLOCKED)", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "ID document type (students/alumni only)", example = "CC")
    private IdType idType;

    @Schema(description = "ID document number", example = "1234567890")
    private String idNumber;

    @Schema(description = "User birth date", example = "2000-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Current academic program (students only)", example = "Ingeniería de Sistemas")
    private String academicProgram;

    @Schema(description = "Current semester (students only)", example = "7")
    private Integer semester;

    @Schema(description = "Associated student ID (guests only)", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String associatedStudentId;

    @Schema(description = "Relationship with associated student (guests only)", example = "Hermano")
    private String relationship;

    @Schema(description = "Graduated academic program (alumni only)", example = "Ingeniería Civil")
    private String formerAcademicProgram;

    @Schema(description = "Account creation timestamp", example = "2026-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-03-20T14:45:00")
    private LocalDateTime updatedAt;
}
