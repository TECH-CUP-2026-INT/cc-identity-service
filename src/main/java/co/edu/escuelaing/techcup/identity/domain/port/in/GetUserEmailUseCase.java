package co.edu.escuelaing.techcup.identity.domain.port.in;

import java.util.UUID;

/**
 * Internal port consumed by am-notification-service to resolve a recipientId
 * to a real email address before sending a notification (RecipientEmailResolver
 * on their side). Notification payloads only carry a UUID, never an email.
 */
public interface GetUserEmailUseCase {

    String getEmailByUserId(UUID userId);
}
