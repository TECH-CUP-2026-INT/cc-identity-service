package co.edu.escuelaing.techcup.identity.dto;

import co.edu.escuelaing.techcup.identity.document.IdType;
import co.edu.escuelaing.techcup.identity.document.UserDocument.UserType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * Request DTO for user self-registration.
 * Covers three registration flows depending on {@code userType}:
 *
 *   TC-01 STUDENT  — institutional email required; academicProgram and semester required.
 *   TC-02 GUEST    — Gmail required; associatedStudentId and relationship required.
 *   TC-03 GRADUATE — institutional or Gmail accepted; academicProgram required; no association needed.
 *
 * Cross-field validation is handled in AuthService to keep the DTO simple.
 */
public class RegisterRequest {

    // ── Common fields (all user types) ────────────────────────────────────

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "User type is required")
    private UserType userType;

    @NotNull(message = "ID type is required")
    private IdType idType;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // ── STUDENT / GRADUATE fields ─────────────────────────────────────────

    /** Required for STUDENT (TC-01) and GRADUATE (TC-03). */
    private String academicProgram;

    /** Required for STUDENT (TC-01). */
    @Positive(message = "Semester must be a positive number")
    private Integer semester;

    // ── GUEST fields ──────────────────────────────────────────────────────

    /**
     * String ID of the registered student this guest is associated with.
     * Required for GUEST (TC-02). Must reference an existing STUDENT in the database.
     */
    private String associatedStudentId;

    /** Relationship between the guest and the associated student (e.g. "Father", "Friend"). */
    private String relationship;

    public RegisterRequest() {}

    public String    getFirstName()           { return firstName; }
    public String    getLastName()            { return lastName; }
    public String    getEmail()               { return email; }
    public String    getPassword()            { return password; }
    public UserType  getUserType()            { return userType; }
    public IdType    getIdType()              { return idType; }
    public String    getIdNumber()            { return idNumber; }
    public LocalDate getDateOfBirth()         { return dateOfBirth; }
    public String    getAcademicProgram()     { return academicProgram; }
    public Integer   getSemester()            { return semester; }
    public String    getAssociatedStudentId() { return associatedStudentId; }
    public String    getRelationship()        { return relationship; }

    public void setFirstName(String firstName)               { this.firstName = firstName; }
    public void setLastName(String lastName)                 { this.lastName = lastName; }
    public void setEmail(String email)                       { this.email = email; }
    public void setPassword(String password)                 { this.password = password; }
    public void setUserType(UserType userType)               { this.userType = userType; }
    public void setIdType(IdType idType)                     { this.idType = idType; }
    public void setIdNumber(String idNumber)                 { this.idNumber = idNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth)        { this.dateOfBirth = dateOfBirth; }
    public void setAcademicProgram(String academicProgram)   { this.academicProgram = academicProgram; }
    public void setSemester(Integer semester)                { this.semester = semester; }
    public void setAssociatedStudentId(String id)            { this.associatedStudentId = id; }
    public void setRelationship(String relationship)         { this.relationship = relationship; }
}
