package co.edu.escuelaing.techcup.identity.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import co.edu.escuelaing.techcup.identity.document.IdType;

public record RefereeRequestDTO(
    @NotBlank @Size(min = 3, max = 100) String fullName,
    @NotNull LocalDate dateOfBirth,
    @NotNull IdType idType,
    @NotBlank @Pattern(regexp = "\\d+") String idNumber, // Regla: Solo dígitos
    @NotBlank @Email String email
) {}