package co.edu.escuelaing.techcup.identity.document;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la entidad UserDocument.
 * Verifica el correcto funcionamiento del Builder, getters y setters.
 * Cubre los campos requeridos para SCRUM-61: Inhabilitar usuario.
 */
class UserDocumentTest {

    /**
     * Verifica que el Builder asigna correctamente todos los campos.
     */
    @Test
    void builder_setsAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        LocalDate dob = LocalDate.of(2000, 5, 15);

        UserDocument user = UserDocument.builder()
                .id(id)
                .email("user@test.com")
                .password("hashed-password")
                .firstName("Juan")
                .lastName("Perez")
                .enabled(true)
                .role(UserDocument.Role.PLAYER)
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
        assertEquals(UserDocument.Role.PLAYER, user.getRole());
        assertEquals(IdType.CC, user.getIdType());
        assertEquals("12345678", user.getIdNumber());
        assertEquals(dob, user.getDateOfBirth());
    }

    /**
     * Verifica que los setters actualizan correctamente los campos.
     */
    @Test
    void setters_updateFieldsCorrectly() {
        UserDocument user = new UserDocument();
        UUID id = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1995, 3, 10);

        user.setId(id);
        user.setEmail("updated@test.com");
        user.setPassword("new-password");
        user.setFirstName("Ana");
        user.setLastName("Gomez");
        user.setEnabled(true);
        user.setRole(UserDocument.Role.REFEREE);
        user.setIdType(IdType.CE);
        user.setIdNumber("87654321");
        user.setDateOfBirth(dob);

        assertEquals(id, user.getId());
        assertEquals("updated@test.com", user.getEmail());
        assertEquals("new-password", user.getPassword());
        assertEquals("Ana", user.getFirstName());
        assertEquals("Gomez", user.getLastName());
        assertTrue(user.isEnabled());
        assertEquals(UserDocument.Role.REFEREE, user.getRole());
        assertEquals(IdType.CE, user.getIdType());
        assertEquals("87654321", user.getIdNumber());
        assertEquals(dob, user.getDateOfBirth());
    }

    /**
     * Verifica que el rol por defecto al construir es PLAYER y enabled es false.
     */
    @Test
    void defaults_arePlayerAndDisabled() {
        UserDocument user = UserDocument.builder()
                .email("default@test.com")
                .build();

        assertEquals(UserDocument.Role.PLAYER, user.getRole());
        assertFalse(user.isEnabled());
    }

    /**
     * Verifica que el enum Role contiene los 5 valores esperados.
     */
    @Test
    void role_enum_hasFiveValues() {
        UserDocument.Role[] roles = UserDocument.Role.values();
        assertEquals(5, roles.length);
        assertEquals(UserDocument.Role.PLAYER,    UserDocument.Role.valueOf("PLAYER"));
        assertEquals(UserDocument.Role.CAPTAIN,   UserDocument.Role.valueOf("CAPTAIN"));
        assertEquals(UserDocument.Role.ADMIN,     UserDocument.Role.valueOf("ADMIN"));
        assertEquals(UserDocument.Role.REFEREE,   UserDocument.Role.valueOf("REFEREE"));
        assertEquals(UserDocument.Role.ORGANIZER, UserDocument.Role.valueOf("ORGANIZER"));
    }

    /**
     * Verifica que setEnabled false deshabilita el usuario correctamente.
     */
    @Test
    void setEnabled_false_disablesUser() {
        UserDocument user = UserDocument.builder()
                .email("active@test.com")
                .enabled(true)
                .build();

        assertTrue(user.isEnabled());
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }
}
