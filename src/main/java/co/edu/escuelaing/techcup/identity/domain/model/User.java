package co.edu.escuelaing.techcup.identity.domain.model;

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
public class User {

    private String id;
    private String fullName;
    private String email;
    private String password;
    private UserType userType;
    private UserRole role;
    private AccountStatus status;
    private IdType idType;
    private String idNumber;
    private LocalDate dateOfBirth;

    // Student-specific
    private String academicProgram;
    private Integer semester;

    // Guest-specific
    private String associatedStudentId;
    private String relationship;

    // Graduate-specific
    private String formerAcademicProgram;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business methods

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public boolean isStudent() {
        return this.userType == UserType.STUDENT;
    }

    public boolean isGuest() {
        return this.userType == UserType.GUEST;
    }

    public boolean isGraduate() {
        return this.userType == UserType.GRADUATE;
    }
}
