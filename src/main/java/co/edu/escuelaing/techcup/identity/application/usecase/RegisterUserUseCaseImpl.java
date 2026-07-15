package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.exception.UserAlreadyExistsException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.RegisterUserUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final AuditEventRepositoryPort auditRepository;
    private final PasswordUtil passwordUtil;

    @Override
    public User createAdminOrOrganizer(User user) {
        log.info("Creating admin/organizer: {}", user.getEmail());
        validateEmailNotExists(user.getEmail());

        if (user.getUserType() == UserType.ADMIN) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setUserType(UserType.ORGANIZER);
            user.setRole(UserRole.ORGANIZER);
        }

        user.setStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordUtil.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        auditRegistration(saved);
        return saved;
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }
    }

    private void auditRegistration(User user) {
        auditRepository.save(AuditEvent.builder()
                .userId(user.getId())
                .actionType(AuditActionType.USER_REGISTERED)
                .description("User registered as " + user.getUserType().name())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
