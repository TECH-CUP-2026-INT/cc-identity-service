package co.edu.escuelaing.techcup.identity.config;

import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OrganizerSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizerSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String organizerEmail = "organizador@escuelaing.edu.co";

        if (!userRepository.existsByEmail(organizerEmail)) {
            UserEntity organizer = UserEntity.builder()
                    .firstName("Organizador")
                    .lastName("TechCup")
                    .email(organizerEmail)
                    .password(passwordEncoder.encode("CambiarEstaClave123!"))
                    .role(UserEntity.Role.ORGANIZER)
                    .enabled(true)
                    .build();

            userRepository.save(organizer);
        }
    }
}