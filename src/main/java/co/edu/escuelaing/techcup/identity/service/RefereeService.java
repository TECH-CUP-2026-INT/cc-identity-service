package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.entity.User;
import co.edu.escuelaing.techcup.identity.entity.UserRole;
import co.edu.escuelaing.techcup.identity.exception.BusinessException;
import co.edu.escuelaing.techcup.identity.repository.UserRepository;
import co.edu.escuelaing.techcup.identity.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefereeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createReferee(RefereeRequestDTO request) {
        log.info("Creando árbitro: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("El email ya está registrado");
        }

        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        User referee = User.builder()
                .id(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .role(UserRole.REFEREE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(referee);
        log.info("Árbitro creado con email: {}", request.getEmail());

        return "Árbitro creado exitosamente. Contraseña temporal: " + temporaryPassword;
    }
}