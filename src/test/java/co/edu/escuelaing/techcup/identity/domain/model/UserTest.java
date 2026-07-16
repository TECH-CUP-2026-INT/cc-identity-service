package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void isActiveReturnsTrueOnlyWhenStatusIsActive() {
        User user = TestFixtures.activeUser();

        assertThat(user.isActive()).isTrue();

        user.setStatus(AccountStatus.INACTIVE);

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void registerFailedLoginAttemptLocksAccountOnceMaxAttemptsReached() {
        User user = TestFixtures.activeUser();

        user.registerFailedLoginAttempt(3, 15);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.isLocked()).isFalse();

        user.registerFailedLoginAttempt(3, 15);
        user.registerFailedLoginAttempt(3, 15);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
        assertThat(user.getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(user.isLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void isLockedReturnsFalseOnceLockWindowHasPassed() {
        User user = TestFixtures.lockedUser();
        user.setLockedUntil(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));

        assertThat(user.isLocked()).isFalse();
    }

    @Test
    void resetFailedLoginAttemptsClearsCounterAndReactivatesLockedAccount() {
        User user = TestFixtures.lockedUser();

        user.resetFailedLoginAttempts();

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void userTypeHelpersIdentifyStudentGuestAndGraduate() {
        User user = TestFixtures.activeUser();

        assertThat(user.isStudent()).isTrue();
        assertThat(user.isGuest()).isFalse();
        assertThat(user.isGraduate()).isFalse();

        user.setUserType(UserType.GUEST);
        assertThat(user.isStudent()).isFalse();
        assertThat(user.isGuest()).isTrue();
        assertThat(user.isGraduate()).isFalse();

        user.setUserType(UserType.GRADUATE);
        assertThat(user.isStudent()).isFalse();
        assertThat(user.isGuest()).isFalse();
        assertThat(user.isGraduate()).isTrue();
    }
}
