package co.edu.escuelaing.techcup.identity.domain.validator;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidEmailDomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailValidatorEdgeCaseTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "student@escuelaing.edu.co",
            "STUDENT@ESCUELAING.EDU.CO",
            "name.lastname@escuelaing.edu.co"
    })
    void institutionalValidationAcceptsCaseInsensitiveInstitutionalDomain(String email) {
        assertThatCode(() -> EmailValidator.validateInstitutionalEmail(email)).doesNotThrowAnyException();
        assertThat(EmailValidator.isInstitutionalEmail(email)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "student@gmail.com",
            "student@fakeescuelaing.edu.co",
            "student@mail.escuelaing.edu.co",
            "student@escuelaing.edu.co ",
            " student@escuelaing.edu.co",
            "student@escuelaing.edu.co.evil.com"
    })
    void institutionalValidationRejectsNullBlankSubdomainsSpacesAndLookalikeDomains(String email) {
        assertThatThrownBy(() -> EmailValidator.validateInstitutionalEmail(email))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessageContaining("escuelaing.edu.co");
        assertThat(EmailValidator.isInstitutionalEmail(email)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "person@gmail.com",
            "PERSON@GMAIL.COM",
            "person.name+tag@gmail.com"
    })
    void gmailValidationAcceptsCaseInsensitiveGmailDomain(String email) {
        assertThatCode(() -> EmailValidator.validateGmailEmail(email)).doesNotThrowAnyException();
        assertThat(EmailValidator.isGmailEmail(email)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "person@googlemail.com",
            "person@gmail.com ",
            "person@mygmail.com",
            "person@gmail.com.evil.co",
            "person@escuelaing.edu.co"
    })
    void gmailValidationRejectsNullBlankLookalikesSpacesAndOtherDomains(String email) {
        assertThatThrownBy(() -> EmailValidator.validateGmailEmail(email))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessageContaining("gmail.com");
        assertThat(EmailValidator.isGmailEmail(email)).isFalse();
    }

    @Test
    void institutionalOrGmailAcceptsBothAllowedDomains() {
        assertThatCode(() -> EmailValidator.validateInstitutionalOrGmail("admin@escuelaing.edu.co"))
                .doesNotThrowAnyException();
        assertThatCode(() -> EmailValidator.validateInstitutionalOrGmail("person@gmail.com"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "person@hotmail.com",
            "person@mail.escuelaing.edu.co",
            "person@gmail.com ",
            "person@fakeescuelaing.edu.co"
    })
    void institutionalOrGmailRejectsAnythingOutsideExactAllowedDomains(String email) {
        assertThatThrownBy(() -> EmailValidator.validateInstitutionalOrGmail(email))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessageContaining("escuelaing.edu.co or gmail.com");
    }
}
