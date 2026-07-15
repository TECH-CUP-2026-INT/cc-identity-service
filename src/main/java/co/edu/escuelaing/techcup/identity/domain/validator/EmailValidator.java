package co.edu.escuelaing.techcup.identity.domain.validator;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidEmailDomainException;

public final class EmailValidator {

    private static final String INSTITUTIONAL_DOMAIN = "escuelaing.edu.co";
    private static final String GMAIL_DOMAIN = "gmail.com";

    private EmailValidator() {}

    public static void validateInstitutionalEmail(String email) {
        if (email == null || !email.toLowerCase().endsWith("@" + INSTITUTIONAL_DOMAIN)) {
            throw new InvalidEmailDomainException(INSTITUTIONAL_DOMAIN);
        }
    }

    public static void validateGmailEmail(String email) {
        if (email == null || !email.toLowerCase().endsWith("@" + GMAIL_DOMAIN)) {
            throw new InvalidEmailDomainException(GMAIL_DOMAIN);
        }
    }

    public static void validateInstitutionalOrGmail(String email) {
        if (email == null) {
            throw new InvalidEmailDomainException(INSTITUTIONAL_DOMAIN + " or " + GMAIL_DOMAIN);
        }
        String lower = email.toLowerCase();
        if (!lower.endsWith("@" + INSTITUTIONAL_DOMAIN) && !lower.endsWith("@" + GMAIL_DOMAIN)) {
            throw new InvalidEmailDomainException(INSTITUTIONAL_DOMAIN + " or " + GMAIL_DOMAIN);
        }
    }

    public static boolean isInstitutionalEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@" + INSTITUTIONAL_DOMAIN);
    }

    public static boolean isGmailEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@" + GMAIL_DOMAIN);
    }
}
