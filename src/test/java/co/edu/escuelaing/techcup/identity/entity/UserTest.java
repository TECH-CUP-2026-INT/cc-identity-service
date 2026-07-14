package co.edu.escuelaing.techcup.identity.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserBuilder_AllFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id("123")
                .email("test@email.com")
                .password("password")
                .name("Test User")
                .role(UserRole.STUDENT)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("123", user.getId());
        assertEquals("test@email.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals(UserRole.STUDENT, user.getRole());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testUserSetters_AllFields() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();
        
        user.setId("456");
        user.setEmail("new@email.com");
        user.setPassword("newpassword");
        user.setName("New User");
        user.setRole(UserRole.ADMIN);
        user.setIsActive(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals("456", user.getId());
        assertEquals("new@email.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("New User", user.getName());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertFalse(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testUser_AllRoles() {
        assertEquals(6, UserRole.values().length);
        assertEquals(UserRole.STUDENT, UserRole.valueOf("STUDENT"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(UserRole.ORGANIZER, UserRole.valueOf("ORGANIZER"));
        assertEquals(UserRole.REFEREE, UserRole.valueOf("REFEREE"));
        assertEquals(UserRole.GUEST, UserRole.valueOf("GUEST"));
        assertEquals(UserRole.GRADUATE, UserRole.valueOf("GRADUATE"));
    }

    @Test
    void testUser_DefaultValues() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getName());
        assertNull(user.getRole());
        assertNull(user.getIsActive());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void testUser_NoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    void testUser_AllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User("789", "all@email.com", "allpass", "All User", UserRole.ORGANIZER, true, now, now);
        
        assertEquals("789", user.getId());
        assertEquals("all@email.com", user.getEmail());
        assertEquals("allpass", user.getPassword());
        assertEquals("All User", user.getName());
        assertEquals(UserRole.ORGANIZER, user.getRole());
        assertTrue(user.getIsActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }
}