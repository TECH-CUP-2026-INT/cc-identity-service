package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.UpdateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateCredentialRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.UpdateRoleRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.UpdateStatusRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoint consumed by users-players-service for credential creation
 * during student, guest, and graduate registration (TC-01, TC-02, TC-03).
 */
@RestController
@RequestMapping("/api/v1/internal/credentials")
@Tag(name = "Internal", description = "Endpoints internos para comunicación entre microservicios. " +
        "Consumido por users-players-service para gestión de credenciales, roles y estados de cuenta. " +
        "Estos endpoints NO deben exponerse al cliente final; están protegidos a nivel de red interna.")
@RequiredArgsConstructor
public class InternalCredentialController {

    private final CreateCredentialsUseCase createCredentialsUseCase;
    private final UpdateCredentialsUseCase updateCredentialsUseCase;
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

    @PutMapping("/{userId}/role")
    @Operation(
            summary = "Actualizar rol de un usuario",
            description = "Actualiza el rol de autenticación de un usuario en Identity Service. " +
                    "Consumido por users-players-service cuando un jugador se promueve a capitán (TC-18) " +
                    "o por teams-service cuando se transfiere la capitanía (TC-27). " +
                    "El cambio se refleja en el próximo JWT generado al hacer login. " +
                    "Registra evento de auditoría ROLE_UPDATED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Rol nulo o inválido."),
            @ApiResponse(responseCode = "404", description = "No se encontraron credenciales para el userId proporcionado.")
    })
    public ResponseEntity<MessageResponse> updateRole(
            @Parameter(description = "ID del usuario (generado por users-players-service)")
            @PathVariable String userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        updateCredentialsUseCase.updateRole(userId, request.getRole());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Role updated to " + request.getRole())
                .build());
    }

    @PutMapping("/{userId}/status")
    @Operation(
            summary = "Actualizar estado de cuenta de un usuario",
            description = "Actualiza el estado de la cuenta de un usuario en Identity Service. " +
                    "Consumido por users-players-service cuando el Admin deshabilita un usuario (TC-19). " +
                    "Una cuenta INACTIVE no puede hacer login. " +
                    "Registra evento de auditoría STATUS_UPDATED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Estado nulo o inválido."),
            @ApiResponse(responseCode = "404", description = "No se encontraron credenciales para el userId proporcionado.")
    })
    public ResponseEntity<MessageResponse> updateStatus(
            @Parameter(description = "ID del usuario (generado por users-players-service)")
            @PathVariable String userId,
            @Valid @RequestBody UpdateStatusRequest request) {
        updateCredentialsUseCase.updateStatus(userId, request.getStatus());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Status updated to " + request.getStatus())
                .build());
    }
}
