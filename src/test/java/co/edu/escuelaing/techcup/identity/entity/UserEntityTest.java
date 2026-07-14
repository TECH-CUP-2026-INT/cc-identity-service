package co.edu.escuelaing.techcup.identity.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
<<<<<<< HEAD
 * SCRUM-13: Pruebas unitarias de la entidad UserEntity con MongoDB.
 * Verifica el builder, setters y valores por defecto.
 */
class UserEntityTest {

    @Test
    void builder_setsAllFieldsCorrectly() {
        String id = UUID.randomUUID().toString();
        LocalDate dob = LocalDate.of(1995, 6, 15);

        UserEntity user = UserEntity.builder()
                .id(id)
                .email("test@gmail.com")
                .password("hashed")
                .firstName("Juan")
                .lastName("Perez")
                .enabled(true)
                .role(UserEntity.Role.REFEREE)
                .dateOfBirth(dob)
                .idType(IdType.CC)
                .idNumber("123456")
                .build();

        assertEquals(id, user.getId());
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("hashed", user.getPassword());
        assertEquals("Juan", user.getFirstName());
        assertEquals("Perez", user.getLastName());
        assertTrue(user.isEnabled());
        assertEquals(UserEntity.Role.REFEREE, user.getRole());
        assertEquals(dob, user.getDateOfBirth());
        assertEquals(IdType.CC, user.getIdType());
        assertEquals("123456", user.getIdNumber());
    }

    @Test
    void setters_updateFieldsCorrectly() {
        UserEntity user = new UserEntity();
        user.setEmail("a@b.com");
        user.setPassword("pass");
        user.setFirstName("Ana");
        user.setLastName("Ruiz");
        user.setEnabled(true);
        user.setRole(UserEntity.Role.ORGANIZER);
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        user.setIdType(IdType.CC);
        user.setIdNumber("999");

        assertEquals("a@b.com", user.getEmail());
        assertEquals("Ana", user.getFirstName());
        assertEquals("Ruiz", user.getLastName());
        assertTrue(user.isEnabled());
        assertEquals(UserEntity.Role.ORGANIZER, user.getRole());
        assertEquals("999", user.getIdNumber());
    }

    @Test
    void defaults_areUserAndDisabled() {
        UserEntity user = new UserEntity();
        assertFalse(user.isEnabled());
        assertEquals(UserEntity.Role.USER, user.getRole());
    }

    @Test
    void role_enum_hasFourValues() {
        assertEquals(4, UserEntity.Role.values().length);
    }

    @Test
    void setEnabled_false_disablesUser() {
        UserEntity user = UserEntity.builder().enabled(true).build();
=======
 * Pruebas unitarias de la entidad UserEntity.
 * Verifica el correcto funcionamiento del Builder, getters y setters.
 * Cubre los campos requeridos para SCRUM-61: Inhabilitar usuario.
 */
class UserEntityTest {

    /**
     * Verifica que el Builder asigna correctamente todos los campos.
     */
    @Test
    void builder_setsAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        LocalDate dob = LocalDate.of(2000, 5, 15);

        UserEntity user = UserEntity.builder()
                .id(id)
                .email("user@test.com")
                .password("hashed-password")
                .firstName("Juan")
                .lastName("Perez")
                .enabled(true)
                .role(UserEntity.Role.USER)
                .idType(IdType.CC)
                .idNumber("12345678")
                .dateOfBirth(dob)
                .build();

        assertEquals(id, user.getId());
        assertEquals("user@test.com", user.getEmail());
        assertEquals("hashed-password", user.getPassword());
        assertEquals("Juan", user.getFirstName());
        assertEquals("Perez", user.getLastName());
        assertTrue(user.isEnabled());
        assertEquals(UserEntity.Role.USER, user.getRole());
        assertEquals(IdType.CC, user.getIdType());
        assertEquals("12345678", user.getIdNumber());
        assertEquals(dob, user.getDateOfBirth());
    }

    /**
     * Verifica que los setters actualizan correctamente los campos.
     */
    @Test
    void setters_updateFieldsCorrectly() {
        UserEntity user = new UserEntity();
        UUID id = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1995, 3, 10);

        user.setId(id);
        user.setEmail("updated@test.com");
        user.setPassword("new-password");
        user.setFirstName("Ana");
        user.setLastName("Gomez");
        user.setEnabled(true);
        user.setRole(UserEntity.Role.REFEREE);
        user.setIdType(IdType.CE);
        user.setIdNumber("87654321");
        user.setDateOfBirth(dob);

        assertEquals(id, user.getId());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals("new-password", user.getPassword());
        assertEquals("Ana", user.getFirstName());
        assertEquals("Gomez", user.getLastName());
        assertTrue(user.isEnabled());
        assertEquals(UserEntity.Role.REFEREE, user.getRole());
        assertEquals(IdType.CE, user.getIdType());
        assertEquals("87654321", user.getIdNumber());
        assertEquals(dob, user.getDateOfBirth());
    }

    /**
     * Verifica que el rol por defecto al construir es USER y enabled es false.
     */
    @Test
    void defaults_areUserAndDisabled() {
        UserEntity user = UserEntity.builder()
                .email("default@test.com")
                .build();

        assertEquals(UserEntity.Role.USER, user.getRole());
        assertFalse(user.isEnabled());
    }

    /**
     * Verifica que el enum Role contiene los 4 valores esperados.
     */
    @Test
    void role_enum_hasFourValues() {
        UserEntity.Role[] roles = UserEntity.Role.values();
        assertEquals(4, roles.length);
        assertEquals(UserEntity.Role.USER, UserEntity.Role.valueOf("USER"));
        assertEquals(UserEntity.Role.ADMIN, UserEntity.Role.valueOf("ADMIN"));
        assertEquals(UserEntity.Role.REFEREE, UserEntity.Role.valueOf("REFEREE"));
        assertEquals(UserEntity.Role.ORGANIZER, UserEntity.Role.valueOf("ORGANIZER"));
    }

    /**
     * Verifica que setEnabled false deshabilita el usuario correctamente.
     */
    @Test
    void setEnabled_false_disablesUser() {
        UserEntity user = UserEntity.builder()
                .email("active@test.com")
                .enabled(true)
                .build();

>>>>>>> origin/develop
        assertTrue(user.isEnabled());
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/develop
