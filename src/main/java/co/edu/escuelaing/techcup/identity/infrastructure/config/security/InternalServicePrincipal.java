package co.edu.escuelaing.techcup.identity.infrastructure.config.security;

/**
 * Principal for service-to-service calls authenticated with the internal API key
 * (currently: am-notification-service resolving a recipient's email). Deliberately
 * carries no userId.
 */
public enum InternalServicePrincipal {
    INSTANCE
}
