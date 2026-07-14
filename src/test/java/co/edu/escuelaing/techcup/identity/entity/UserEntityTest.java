package co.edu.escuelaing.techcup.identity.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
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
                .role(UserEntity.Role.PLAYER)
                .idType(IdType.CC)
                .idNumber("12345678")
                .dateOfBirth(dob)
                .build();

        assertEquals(id.toString(), user.getId());
        assertEquals("user@test.com", user.getEmail());
        assertEquals("hashed-password", user.getPassword());
        assertEquals("Juan", user.getFirstName());
        assertEquals("Perez", user.getLastName());
        assertTrue(user.isEnabled());
        assertEquals(UserEntity.Role.PLAYER, user.getRole());
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

        user.setId(id.toString());
        user.setEmail("updated@test.com");
        user.setPassword("new-password");
        user.setFirstName("Ana");
        user.setLastName("Gomez");
        user.setEnabled(true);
        user.setRole(UserEntity.Role.REFEREE);
        user.setIdType(IdType.CE);
        user.setIdNumber("87654321");
        user.setDateOfBirth(dob);

        assertEquals(id.toString(), user.getId());
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
     * Verifica que el rol por defecto al construir es PLAYER y enabled es false.
     */
    @Test
    void defaults_arePlayerAndDisabled() {
        UserEntity user = UserEntity.builder()
                .email("default@test.com")
                .build();

        assertEquals(UserEntity.Role.PLAYER, user.getRole());
        assertFalse(user.isEnabled());
    }

    /**
     * Verifica que el enum Role contiene los 5 valores esperados.
     */
    @Test
    void role_enum_hasFiveValues() {
        UserEntity.Role[] roles = UserEntity.Role.values();
        assertEquals(5, roles.length);
        assertEquals(UserEntity.Role.PLAYER,    UserEntity.Role.valueOf("PLAYER"));
        assertEquals(UserEntity.Role.CAPTAIN,   UserEntity.Role.valueOf("CAPTAIN"));
        assertEquals(UserEntity.Role.ADMIN,     UserEntity.Role.valueOf("ADMIN"));
        assertEquals(UserEntity.Role.REFEREE,   UserEntity.Role.valueOf("REFEREE"));
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

        assertTrue(user.isEnabled());
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }
}
