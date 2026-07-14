package co.edu.escuelaing.techcup.identity.dto;

import co.edu.escuelaing.techcup.identity.entity.IdType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RefereeRequestDTO(
    @NotBlank @Size(min = 3, max = 100) String fullName,
    @NotNull LocalDate dateOfBirth,
    @NotNull IdType idType,
    @NotBlank @Pattern(regexp = "\\d+") String idNumber, // Regla: Solo dígitos
    @NotBlank @Email String email
) {}