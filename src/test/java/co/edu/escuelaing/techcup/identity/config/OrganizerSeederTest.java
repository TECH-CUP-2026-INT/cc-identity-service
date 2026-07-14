package co.edu.escuelaing.techcup.identity.config;

import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrganizerSeederTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OrganizerSeeder seeder;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        seeder = new OrganizerSeeder(userRepository, passwordEncoder);
    }

    @Test
    void createsOrganizerWhenNotExists() throws Exception {
        when(userRepository.existsByEmail("organizador@escuelaing.edu.co")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        seeder.run();

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void doesNotDuplicateOrganizerWhenAlreadyExists() throws Exception {
        when(userRepository.existsByEmail("organizador@escuelaing.edu.co")).thenReturn(true);

        seeder.run();

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}