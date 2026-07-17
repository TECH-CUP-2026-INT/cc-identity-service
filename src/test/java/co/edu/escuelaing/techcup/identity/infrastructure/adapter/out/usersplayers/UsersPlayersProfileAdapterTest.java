package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.usersplayers;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.exception.UserProfileUnavailableException;
import co.edu.escuelaing.techcup.identity.domain.model.UserProfileSnapshot;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersPlayersProfileAdapterTest {

    @Mock
    private UsersPlayersFeignClient usersPlayersFeignClient;

    @Test
    void fetchProfileMapsRoleAndStatus() {
        UUID userId = UUID.randomUUID();
        when(usersPlayersFeignClient.getProfile(userId)).thenReturn(new PlayerProfileResponse("CAPTAIN", "ACTIVE"));
        UsersPlayersProfileAdapter adapter = new UsersPlayersProfileAdapter(usersPlayersFeignClient);

        UserProfileSnapshot snapshot = adapter.fetchProfile(userId);

        assertThat(snapshot.getRole()).isEqualTo(UserRole.CAPTAIN);
        assertThat(snapshot.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(snapshot.isActive()).isTrue();
    }

    @Test
    void fetchProfileThrowsWhenFeignFails() {
        UUID userId = UUID.randomUUID();
        when(usersPlayersFeignClient.getProfile(userId)).thenThrow(mock(FeignException.class));
        UsersPlayersProfileAdapter adapter = new UsersPlayersProfileAdapter(usersPlayersFeignClient);

        assertThatThrownBy(() -> adapter.fetchProfile(userId))
                .isInstanceOf(UserProfileUnavailableException.class);
    }

    @Test
    void fetchProfileThrowsWhenRoleIsUnrecognized() {
        UUID userId = UUID.randomUUID();
        when(usersPlayersFeignClient.getProfile(userId)).thenReturn(new PlayerProfileResponse("UNKNOWN_ROLE", "ACTIVE"));
        UsersPlayersProfileAdapter adapter = new UsersPlayersProfileAdapter(usersPlayersFeignClient);

        assertThatThrownBy(() -> adapter.fetchProfile(userId))
                .isInstanceOf(UserProfileUnavailableException.class);
    }
}
