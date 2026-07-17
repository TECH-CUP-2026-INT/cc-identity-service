package co.edu.escuelaing.techcup.identity.application.usecase;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.exception.UserNotFoundException;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.UpdateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.out.AuditEventRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateCredentialsUseCaseImpl implements UpdateCredentialsUseCase {

    private final UserRepositoryPort userRepository;
    private final AuditEventRepositoryPort auditRepository;

    @Override
    public void updateRole(UUID userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        UserRole previousRole = user.getRole();
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        log.info("Role updated for userId {}: {} -> {}", userId, previousRole, role);
        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.ROLE_UPDATED)
                .description("Role updated from " + previousRole + " to " + role)
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }

    @Override
    public void updateStatus(UUID userId, AccountStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        AccountStatus previousStatus = user.getStatus();
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        log.info("Status updated for userId {}: {} -> {}", userId, previousStatus, status);
        auditRepository.save(AuditEvent.builder()
                .userId(userId)
                .actionType(AuditActionType.STATUS_UPDATED)
                .description("Status updated from " + previousStatus + " to " + status)
                .success(true)
                .timestamp(LocalDateTime.now(ZoneOffset.UTC))
                .build());
    }
}
