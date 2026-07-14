package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefereeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createReferee(RefereeRequestDTO request) {
        log.info("Creando árbitro: {}", request.email());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("El email ya está registrado");
        }

        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        String fullName = request.fullName();
        String firstName = fullName;
        String lastName = "";
        int spaceIndex = fullName.indexOf(' ');
        if (spaceIndex > 0) {
            firstName = fullName.substring(0, spaceIndex);
            lastName = fullName.substring(spaceIndex + 1);
        }

        UserEntity referee = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .email(request.email())
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .role(UserEntity.Role.REFEREE)
                .userType(UserEntity.UserType.GRADUATE)
                .enabled(true)
                .build();

        userRepository.save(referee);
        log.info("Árbitro creado con email: {}", request.email());

        return "Árbitro creado exitosamente. Contraseña temporal: " + temporaryPassword;
    }
}