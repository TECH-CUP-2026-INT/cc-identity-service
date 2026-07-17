package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.usersplayers;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.exception.UserProfileUnavailableException;
import co.edu.escuelaing.techcup.identity.domain.model.UserProfileSnapshot;
import co.edu.escuelaing.techcup.identity.domain.port.out.UserProfilePort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersPlayersProfileAdapter implements UserProfilePort {

    private final UsersPlayersFeignClient usersPlayersFeignClient;

    @Override
    public UserProfileSnapshot fetchProfile(UUID userId) {
        try {
            PlayerProfileResponse response = usersPlayersFeignClient.getProfile(userId);
            return new UserProfileSnapshot(
                    UserRole.valueOf(response.rol()),
                    AccountStatus.valueOf(response.estado()));
        } catch (FeignException | IllegalArgumentException e) {
            log.error("Could not fetch profile for user {} from users-players-service: {}", userId, e.getMessage());
            throw new UserProfileUnavailableException(userId.toString());
        }
    }
}
