package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEdgeCaseTest {

    @Test
    void helperMethodsReturnFalseWhenStatusOrUserTypeIsNull() {
        User user = TestFixtures.activeUser();
        user.setStatus(null);
        user.setUserType(null);

        assertThat(user.isActive()).isFalse();
        assertThat(user.isStudent()).isFalse();
        assertThat(user.isGuest()).isFalse();
        assertThat(user.isGraduate()).isFalse();
    }

    @Test
    void isActiveReturnsFalseForInactiveStatus() {
        User user = TestFixtures.activeUser();

        user.setStatus(AccountStatus.INACTIVE);

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void isLockedReturnsFalseWhenStatusIsLockedButLockedUntilIsNull() {
        User user = TestFixtures.activeUser();
        user.setStatus(AccountStatus.LOCKED);
        user.setLockedUntil(null);

        assertThat(user.isLocked()).isFalse();
    }

    @Test
    void isLockedReturnsFalseWhenStatusIsNotLockedEvenWithFutureLockedUntil() {
        User user = TestFixtures.activeUser();
        user.setLockedUntil(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(10));

        assertThat(user.isLocked()).isFalse();
    }
}
