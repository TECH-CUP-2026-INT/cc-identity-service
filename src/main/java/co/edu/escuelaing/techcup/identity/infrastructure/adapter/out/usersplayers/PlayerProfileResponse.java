package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.usersplayers;

/**
 * Subconjunto de campos que identity necesita de
 * GET /internal/players/{userId}/profile en users-players-service.
 * El resto de campos que trae esa respuesta se ignoran en la deserialización.
 */
public record PlayerProfileResponse(String rol, String estado) {
}
