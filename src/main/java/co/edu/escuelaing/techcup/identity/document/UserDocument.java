package co.edu.escuelaing.techcup.identity.document;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a registered user stored in the MongoDB users collection.
 * Each user has a unique email, encrypted password, role, userType and account status.
 */
@Document(collection = "users")
public class UserDocument {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private boolean enabled = false;

    private Role role = Role.PLAYER;

    private UserType userType;

    private IdType idType;

    @Indexed(unique = true, sparse = true)
    private String idNumber;

    private LocalDate dateOfBirth;

    private String academicProgram;

    private Integer semester;

    private String associatedStudentId;

    private String relationship;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

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

    public UserDocument() {
    }

    private UserDocument(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.enabled = builder.enabled;
        this.role = builder.role;
        this.userType = builder.userType;
        this.idType = builder.idType;
        this.idNumber = builder.idNumber;
        this.dateOfBirth = builder.dateOfBirth;
        this.academicProgram = builder.academicProgram;
        this.semester = builder.semester;
        this.associatedStudentId = builder.associatedStudentId;
        this.relationship = builder.relationship;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID id;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private boolean enabled = false;
        private Role role = Role.PLAYER;
        private UserType userType;
        private IdType idType;
        private String idNumber;
        private LocalDate dateOfBirth;
        private String academicProgram;
        private Integer semester;
        private String associatedStudentId;
        private String relationship;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder userType(UserType userType) {
            this.userType = userType;
            return this;
        }

        public Builder idType(IdType idType) {
            this.idType = idType;
            return this;
        }

        public Builder idNumber(String idNumber) {
            this.idNumber = idNumber;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder academicProgram(String academicProgram) {
            this.academicProgram = academicProgram;
            return this;
        }

        public Builder semester(Integer semester) {
            this.semester = semester;
            return this;
        }

        public Builder associatedStudentId(String associatedStudentId) {
            this.associatedStudentId = associatedStudentId;
            return this;
        }

        public Builder relationship(String relationship) {
            this.relationship = relationship;
            return this;
        }

        public UserDocument build() {
            return new UserDocument(this);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Role getRole() {
        return role;
    }

    public UserType getUserType() {
        return userType;
    }

    public IdType getIdType() {
        return idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAcademicProgram() {
        return academicProgram;
    }

    public Integer getSemester() {
        return semester;
    }

    public String getAssociatedStudentId() {
        return associatedStudentId;
    }

    public String getRelationship() {
        return relationship;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAcademicProgram(String academicProgram) {
        this.academicProgram = academicProgram;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public void setAssociatedStudentId(String associatedStudentId) {
        this.associatedStudentId = associatedStudentId;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
