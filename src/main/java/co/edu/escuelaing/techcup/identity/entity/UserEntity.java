package co.edu.escuelaing.techcup.identity.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un usuario registrado en el sistema.
 * Mapeado a la coleccion {@code users} en MongoDB.
 * Cada usuario tiene un email unico, password hasheado y un rol
 * que controla su nivel de acceso.
 *
 * Las cuentas inician deshabilitadas hasta que el usuario completa la verificacion OTP (SCRUM-13).
 * Los timestamps son gestionados automaticamente por Spring Data MongoDB (@EnableMongoAuditing).
 *
 * @see OtpCodeEntity
 */
@Document(collection = "users")
public class UserEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String firstName;
    private String lastName;
    private boolean enabled = false;
    private Role role = Role.USER;

    /** SCRUM-22: Campos requeridos para registro de arbitros. */
    private LocalDate dateOfBirth;
    private IdType idType;

    @Indexed(unique = true, sparse = true)
    private String idNumber;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * SCRUM-22: Roles ampliados para soportar arbitros y organizadores.
     * SCRUM-61: ADMIN no puede ser inhabilitado.
     */
    public enum Role { USER, ADMIN, ORGANIZER, REFEREE }

    public UserEntity() {}

    /**
     * Constructor privado usado exclusivamente por el Builder.
     * @param builder instancia del builder con los valores de los campos
     */
    private UserEntity(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.enabled = builder.enabled;
        this.role = builder.role;
        this.dateOfBirth = builder.dateOfBirth;
        this.idType = builder.idType;
        this.idNumber = builder.idNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private boolean enabled = false;
        private Role role = Role.USER;
        private LocalDate dateOfBirth;
        private IdType idType;
        private String idNumber;

        public Builder id(String id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder dateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; return this; }
        public Builder idType(IdType idType) { this.idType = idType; return this; }
        public Builder idNumber(String idNumber) { this.idNumber = idNumber; return this; }

        public UserEntity build() { return new UserEntity(this); }
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isEnabled() { return enabled; }
    public Role getRole() { return role; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public IdType getIdType() { return idType; }
    public String getIdNumber() { return idNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setRole(Role role) { this.role = role; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setIdType(IdType idType) { this.idType = idType; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
}
