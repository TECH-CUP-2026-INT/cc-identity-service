package co.edu.escuelaing.techcup.identity.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 
 * Represents a registered user in the system
 * Maps to the {@code users} table in the database, 
 * Each user has a unique email, a hashed password, and a rolethat controls their access level within the application
 * 
 * Accounts start disabled (enabled = false) until the user completes OTP email verification (SCRUM-13 JIRA)
 * Timestamps (createdAt, updatedAt) are managed automatically by Hibernate and should never be set manually.
 *  @see OtpCodeEntity
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type")
    private IdType idType;

    @Column(name = "id_number", unique = true)
    private String idNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Role { USER, ADMIN, REFEREE, ORGANIZER }

    public UserEntity() {}
    /**
     * 
     * @param builder is the builder instance containing the field values
     */

    private UserEntity (Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.enabled = builder.enabled;
        this.role = builder.role;
        this.idType = builder.idType;
        this.idNumber = builder.idNumber;
        this.dateOfBirth = builder.dateOfBirth;
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
        private Role role = Role.USER;
        private IdType idType;
        private String idNumber;
        private LocalDate dateOfBirth;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder idType(IdType idType) { this.idType = idType; return this; }
        public Builder idNumber(String idNumber) { this.idNumber = idNumber; return this; }
        public Builder dateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; return this; }

        public UserEntity build() { return new UserEntity(this); }
    }
    /**
     * Getters
     * @return
     */
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isEnabled() { return enabled; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public IdType getIdType() { return idType; }
    public String getIdNumber() { return idNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    /**
     * Setters
     * @param id
     */
    public void setId(UUID id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setRole(Role role) { this.role = role; }
    public void setIdType(IdType idType) { this.idType = idType; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}