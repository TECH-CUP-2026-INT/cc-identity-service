package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.GetUserEmailUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.RevokeUserSessionsUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateCredentialRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserEmailResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal endpoint consumed by users-players-service for credential creation
 * during Student User Registration, Guest User Registration, and Graduate User Registration.
 * Role and status are no longer pushed here: users-players-service is the source of
 * truth for both, and identity queries GET /internal/players/{userId}/profile there
 * live (at login and OTP validation) instead of keeping its own synced copy.
 */
@RestController
@RequestMapping("/api/v1/internal/credentials")
@Tag(name = "Internal", description = "Endpoints internos para comunicación entre microservicios. " +
        "Consumido por users-players-service para gestión de credenciales, roles y estados de cuenta. " +
        "Estos endpoints NO deben exponerse al cliente final; están protegidos a nivel de red interna.")
@Hidden
@RequiredArgsConstructor
public class InternalCredentialController {

    private final CreateCredentialsUseCase createCredentialsUseCase;
    private final GetUserEmailUseCase getUserEmailUseCase;
    private final RevokeUserSessionsUseCase revokeUserSessionsUseCase;
    private final UserMapper userMapper;

    @PostMapping
    @Operation(
            summary = "Crear credenciales para usuario registrado en users-players-service",
            description = "Crea las credenciales de autenticación (email + contraseña con hash BCrypt) para un usuario que fue " +
                    "registrado previamente en el users-players-service. Recibe el userId generado por users-players-service " +
                    "(fuente de verdad), email, contraseña en texto plano, nombre completo, " +
                    "tipo de usuario y rol. La cuenta se crea en estado ACTIVE. Si el email ya existe, retorna 409 Conflict. " +
                    "Envía OTP al correo del usuario. Registra evento de auditoría CREDENTIALS_CREATED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credenciales creadas exitosamente. Retorna datos del usuario."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: userId vacío, email vacío/formato incorrecto, contraseña vacía, nombre vacío, tipo/rol nulos."),
            @ApiResponse(responseCode = "409", description = "Ya existen credenciales para el correo proporcionado.")
    })
    public ResponseEntity<UserResponse> createCredentials(@Valid @RequestBody CreateCredentialRequest request) {
        User saved = createCredentialsUseCase.createCredentials(
                request.getUserId(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getUserType(),
                request.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }

    @GetMapping("/{userId}/email")
    @Operation(
            summary = "Consultar el correo de un usuario por su userId",
            description = "Resuelve el correo electrónico real asociado a un userId. " +
                    "Consumido por am-notification-service para poder enviar el correo de una notificación, " +
                    "ya que los eventos que le llegan solo traen el recipientId (UUID), nunca el email. " +
                    "Protegido con API key interna (header X-Internal-Api-Key), a diferencia del resto de " +
                    "endpoints internos de este controller."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo encontrado exitosamente."),
            @ApiResponse(responseCode = "401", description = "API key interna ausente o inválida."),
            @ApiResponse(responseCode = "404", description = "No se encontraron credenciales para el userId proporcionado.")
    })
    public ResponseEntity<UserEmailResponse> getUserEmail(
            @Parameter(description = "ID del usuario (generado por users-players-service)")
            @PathVariable UUID userId) {
        String email = getUserEmailUseCase.getEmailByUserId(userId);
        return ResponseEntity.ok(UserEmailResponse.builder()
                .email(email)
                .build());
    }

    @PostMapping("/{userId}/revoke-sessions")
    @Operation(
            summary = "Revocar todas las sesiones activas de un usuario",
            description = "Invalida de inmediato todos los JWT activos de un usuario. Consumido por " +
                    "users-players-service justo después de deshabilitar una cuenta (Deshabilitación de Usuario), " +
                    "para que el corte de acceso sea inmediato en vez de esperar a que el JWT expire por su cuenta. " +
                    "Reutiliza la misma infraestructura de revocación que el logout."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesiones revocadas exitosamente (o no había ninguna activa).")
    })
    public ResponseEntity<MessageResponse> revokeSessions(
            @Parameter(description = "ID del usuario (generado por users-players-service)")
            @PathVariable UUID userId) {
        revokeUserSessionsUseCase.revokeAllSessions(userId);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Sessions revoked for user " + userId)
                .build());
    }
}
