package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

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
