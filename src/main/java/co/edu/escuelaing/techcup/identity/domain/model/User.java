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
import java.time.ZoneOffset;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
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

    @Builder.Default
    private int failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business methods

    public boolean isActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.status == AccountStatus.LOCKED
                && this.lockedUntil != null
                && LocalDateTime.now(ZoneOffset.UTC).isBefore(this.lockedUntil);
    }

    public void registerFailedLoginAttempt(int maxAttempts, int lockoutDurationMinutes) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maxAttempts) {
            this.status = AccountStatus.LOCKED;
            this.lockedUntil = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(lockoutDurationMinutes);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        if (this.status == AccountStatus.LOCKED) {
            this.status = AccountStatus.ACTIVE;
        }
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
