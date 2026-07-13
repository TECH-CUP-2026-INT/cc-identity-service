package co.edu.escuelaing.techcup.identity.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a registered user in the system.
 * Maps to the {@code users} table in the database.
 *
 * Supports three registration types (TC-01, TC-02, TC-03):
 *   - STUDENT : must use institutional email; academicProgram and semester required.
 *   - GUEST   : must use Gmail; associatedStudentId and relationship required.
 *   - GRADUATE: institutional email preferred, Gmail accepted; academicProgram required.
 *
 * Accounts start disabled (enabled = false) until OTP email verification is completed (TC-10).
 *
 * @see OtpCodeEntity
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean enabled = false;

    /**
     * Platform role assigned to this user.
     * USER is kept as the legacy default; PLAYER is the functional player role.
     * Referees and organizers are assigned their roles directly on creation.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.PLAYER;

    /**
     * Registration type that determines which email and fields are required.
     * TC-01 → STUDENT | TC-02 → GUEST | TC-03 → GRADUATE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type")
    private IdType idType;

    @Column(name = "id_number", unique = true)
    private String idNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Academic program. Required for STUDENT and GRADUATE (TC-01, TC-03). */
    @Column(name = "academic_program")
    private String academicProgram;

    /** Current semester. Required for STUDENT only (TC-01). */
    @Column(name = "semester")
    private Integer semester;

    /**
     * UUID of the registered student this guest is associated with.
     * Required for GUEST (TC-02). Must reference an existing STUDENT user.
     */
    @Column(name = "associated_student_id")
    private UUID associatedStudentId;

    /**
     * Relationship description between the guest and the associated student.
     * Required for GUEST (TC-02). E.g. "Father", "Friend".
     */
    @Column(name = "relationship")
    private String relationship;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Enums ──────────────────────────────────────────────────────────────

    public enum Role {
        PLAYER,
        CAPTAIN,
        ORGANIZER,
        ADMIN,
        REFEREE
    }

    public enum UserType {
        STUDENT,
        GUEST,
        GRADUATE
    }

    // ── Constructors ───────────────────────────────────────────────────────

    public UserEntity() {}

    private UserEntity(Builder builder) {
        this.id                  = builder.id;
        this.email               = builder.email;
        this.password            = builder.password;
        this.firstName           = builder.firstName;
        this.lastName            = builder.lastName;
        this.enabled             = builder.enabled;
        this.role                = builder.role;
        this.userType            = builder.userType;
        this.idType              = builder.idType;
        this.idNumber            = builder.idNumber;
        this.dateOfBirth         = builder.dateOfBirth;
        this.academicProgram     = builder.academicProgram;
        this.semester            = builder.semester;
        this.associatedStudentId = builder.associatedStudentId;
        this.relationship        = builder.relationship;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ── Builder ────────────────────────────────────────────────────────────

    public static class Builder {
        private UUID        id;
        private String      email;
        private String      password;
        private String      firstName;
        private String      lastName;
        private boolean     enabled          = false;
        private Role        role             = Role.PLAYER;
        private UserType    userType;
        private IdType      idType;
        private String      idNumber;
        private LocalDate   dateOfBirth;
        private String      academicProgram;
        private Integer     semester;
        private UUID        associatedStudentId;
        private String      relationship;

        public Builder id(UUID id)                               { this.id = id; return this; }
        public Builder email(String email)                       { this.email = email; return this; }
        public Builder password(String password)                 { this.password = password; return this; }
        public Builder firstName(String firstName)               { this.firstName = firstName; return this; }
        public Builder lastName(String lastName)                 { this.lastName = lastName; return this; }
        public Builder enabled(boolean enabled)                  { this.enabled = enabled; return this; }
        public Builder role(Role role)                           { this.role = role; return this; }
        public Builder userType(UserType userType)               { this.userType = userType; return this; }
        public Builder idType(IdType idType)                     { this.idType = idType; return this; }
        public Builder idNumber(String idNumber)                 { this.idNumber = idNumber; return this; }
        public Builder dateOfBirth(LocalDate dateOfBirth)        { this.dateOfBirth = dateOfBirth; return this; }
        public Builder academicProgram(String academicProgram)   { this.academicProgram = academicProgram; return this; }
        public Builder semester(Integer semester)                { this.semester = semester; return this; }
        public Builder associatedStudentId(UUID id)              { this.associatedStudentId = id; return this; }
        public Builder relationship(String relationship)         { this.relationship = relationship; return this; }

        public UserEntity build() { return new UserEntity(this); }
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public UUID        getId()                  { return id; }
    public String      getEmail()               { return email; }
    public String      getPassword()            { return password; }
    public String      getFirstName()           { return firstName; }
    public String      getLastName()            { return lastName; }
    public boolean     isEnabled()              { return enabled; }
    public Role        getRole()                { return role; }
    public UserType    getUserType()             { return userType; }
    public IdType      getIdType()              { return idType; }
    public String      getIdNumber()            { return idNumber; }
    public LocalDate   getDateOfBirth()         { return dateOfBirth; }
    public String      getAcademicProgram()     { return academicProgram; }
    public Integer     getSemester()            { return semester; }
    public UUID        getAssociatedStudentId() { return associatedStudentId; }
    public String      getRelationship()        { return relationship; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }

    // ── Setters ────────────────────────────────────────────────────────────

    public void setId(UUID id)                               { this.id = id; }
    public void setEmail(String email)                       { this.email = email; }
    public void setPassword(String password)                 { this.password = password; }
    public void setFirstName(String firstName)               { this.firstName = firstName; }
    public void setLastName(String lastName)                 { this.lastName = lastName; }
    public void setEnabled(boolean enabled)                  { this.enabled = enabled; }
    public void setRole(Role role)                           { this.role = role; }
    public void setUserType(UserType userType)               { this.userType = userType; }
    public void setIdType(IdType idType)                     { this.idType = idType; }
    public void setIdNumber(String idNumber)                 { this.idNumber = idNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth)        { this.dateOfBirth = dateOfBirth; }
    public void setAcademicProgram(String academicProgram)   { this.academicProgram = academicProgram; }
    public void setSemester(Integer semester)                { this.semester = semester; }
    public void setAssociatedStudentId(UUID id)              { this.associatedStudentId = id; }
    public void setRelationship(String relationship)         { this.relationship = relationship; }
}