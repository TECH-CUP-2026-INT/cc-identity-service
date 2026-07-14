package co.edu.escuelaing.techcup.identity.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SCRUM-13: Pruebas unitarias de la entidad UserEntity.
 * Verifica el builder, setters y valores por defecto.
 */
class UserEntityTest {

    @Test
    void builder_setsAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
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
        UserEntity.Role[] roles = UserEntity.Role.values();
        assertEquals(4, roles.length);
    }

    @Test
    void setEnabled_false_disablesUser() {
        UserEntity user = UserEntity.builder().enabled(true).build();
        assertTrue(user.isEnabled());
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }
}
