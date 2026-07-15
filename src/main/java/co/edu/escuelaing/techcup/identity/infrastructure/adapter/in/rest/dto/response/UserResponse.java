package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.IdType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
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
public class UserResponse {

    private String id;
    private String fullName;
    private String email;
    private UserType userType;
    private UserRole role;
    private AccountStatus status;
    private IdType idType;
    private String idNumber;
    private LocalDate dateOfBirth;
    private String academicProgram;
    private Integer semester;
    private String associatedStudentId;
    private String relationship;
    private String formerAcademicProgram;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
