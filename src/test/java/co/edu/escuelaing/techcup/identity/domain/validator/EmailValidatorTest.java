package co.edu.escuelaing.techcup.identity.domain.validator;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidEmailDomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailValidatorTest {

    @Test
    void validatesInstitutionalEmailsCaseInsensitively() {
        EmailValidator.validateInstitutionalEmail("student@ESCuelaing.edu.co");

        assertThat(EmailValidator.isInstitutionalEmail("student@escuelaing.edu.co")).isTrue();
        assertThat(EmailValidator.isInstitutionalEmail("student@gmail.com")).isFalse();
    }

    @Test
    void rejectsNonInstitutionalEmails() {
        assertThatThrownBy(() -> EmailValidator.validateInstitutionalEmail("person@gmail.com"))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessageContaining("escuelaing.edu.co");

        assertThatThrownBy(() -> EmailValidator.validateInstitutionalEmail(null))
                .isInstanceOf(InvalidEmailDomainException.class);
    }

    @Test
    void validatesGmailEmailsCaseInsensitively() {
        EmailValidator.validateGmailEmail("person@GMAIL.com");

        assertThat(EmailValidator.isGmailEmail("person@gmail.com")).isTrue();
        assertThat(EmailValidator.isGmailEmail("person@escuelaing.edu.co")).isFalse();
    }

    @Test
    void rejectsNonGmailEmails() {
        assertThatThrownBy(() -> EmailValidator.validateGmailEmail("student@escuelaing.edu.co"))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessageContaining("gmail.com");
    }

    @Test
    void validatesInstitutionalOrGmailEmails() {
        EmailValidator.validateInstitutionalOrGmail("student@escuelaing.edu.co");
        EmailValidator.validateInstitutionalOrGmail("person@gmail.com");

        assertThatThrownBy(() -> EmailValidator.validateInstitutionalOrGmail("person@example.com"))
                .isInstanceOf(InvalidEmailDomainException.class)
                .hasMessageContaining("escuelaing.edu.co or gmail.com");

        assertThatThrownBy(() -> EmailValidator.validateInstitutionalOrGmail(null))
                .isInstanceOf(InvalidEmailDomainException.class);
    }
}
