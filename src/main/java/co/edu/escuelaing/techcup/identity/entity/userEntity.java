package co.edu.escuelaing.techcup.identity.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "users")
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

public class userEntity {
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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Role { USER, ADMIN }

    // Constructor 
    public userEntity() {}
    /**
     * 
     * @param builder is the builder instance containing the field values
     */

    private userEntity (Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.enabled = builder.enabled;
        this.role = builder.role;
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

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder role(Role role) { this.role = role; return this; }

        public userEntity build() { return new userEntity(this); }
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
}