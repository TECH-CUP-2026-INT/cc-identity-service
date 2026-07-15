package co.edu.escuelaing.techcup.identity.domain.validator;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidEmailDomainException;

public final class EmailValidator {

    private static final String INSTITUTIONAL_DOMAIN = "escuelaing.edu.co";
    private static final String GMAIL_DOMAIN = "gmail.com";

    private EmailValidator() {}

    public static void validateInstitutionalEmail(String email) {
        if (!isInstitutionalEmail(email)) {
            throw new InvalidEmailDomainException(INSTITUTIONAL_DOMAIN);
        }
    }

    public static void validateGmailEmail(String email) {
        if (!isGmailEmail(email)) {
            throw new InvalidEmailDomainException(GMAIL_DOMAIN);
        }
    }

    public static void validateInstitutionalOrGmail(String email) {
        if (!isInstitutionalEmail(email) && !isGmailEmail(email)) {
            throw new InvalidEmailDomainException(INSTITUTIONAL_DOMAIN + " or " + GMAIL_DOMAIN);
        }
    }

    public static boolean isInstitutionalEmail(String email) {
        return email != null && !email.contains(" ") && email.toLowerCase().endsWith("@" + INSTITUTIONAL_DOMAIN);
    }

    public static boolean isGmailEmail(String email) {
        return email != null && !email.contains(" ") && email.toLowerCase().endsWith("@" + GMAIL_DOMAIN);
    }
}
