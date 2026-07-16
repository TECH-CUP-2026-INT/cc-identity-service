package co.edu.escuelaing.techcup.identity.domain.model;

import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SessionActivityTest {

    @Test
    void freshActivityIsNotExpiredByInactivity() {
        SessionActivity activity = TestFixtures.sessionActivity();

        assertThat(activity.isExpiredByInactivity(30)).isFalse();
    }

    @Test
    void activityOlderThanTimeoutIsExpiredByInactivity() {
        SessionActivity activity = TestFixtures.sessionActivity();
        activity.setLastActivityAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(31));

        assertThat(activity.isExpiredByInactivity(30)).isTrue();
    }

    @Test
    void touchUpdatesLastActivityToNow() {
        SessionActivity activity = TestFixtures.sessionActivity();
        activity.setLastActivityAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(20));

        activity.touch();

        assertThat(activity.isExpiredByInactivity(30)).isFalse();
        assertThat(activity.getLastActivityAt()).isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(5));
    }
}
