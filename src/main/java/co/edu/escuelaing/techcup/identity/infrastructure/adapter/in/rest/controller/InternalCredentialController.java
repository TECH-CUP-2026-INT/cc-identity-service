package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateCredentialRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
        "Consumido por users-players-service durante el registro de estudiantes (TC-01), invitados (TC-02) y egresados (TC-03). " +
        "Estos endpoints NO deben exponerse al cliente final; están protegidos a nivel de red interna.")
@RequiredArgsConstructor
public class InternalCredentialController {

    private final CreateCredentialsUseCase createCredentialsUseCase;
    private final UserMapper userMapper;

    @PostMapping
    @Operation(
            summary = "Crear credenciales para usuario registrado en users-players-service",
            description = "Crea las credenciales de autenticación (email + contraseña con hash BCrypt) para un usuario que fue " +
                    "registrado previamente en el users-players-service. Recibe email, contraseña en texto plano, nombre completo, " +
                    "tipo de usuario (STUDENT, GUEST, GRADUATE) y rol (PLAYER, VIEWER). " +
                    "La cuenta se crea en estado ACTIVE. Si el email ya existe, retorna 409 Conflict. " +
                    "Registra evento de auditoría USER_REGISTERED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credenciales creadas exitosamente. Retorna datos del usuario."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: email vacío/formato incorrecto, contraseña vacía, nombre vacío, tipo/rol nulos."),
            @ApiResponse(responseCode = "409", description = "Ya existen credenciales para el correo proporcionado.")
    })
    public ResponseEntity<UserResponse> createCredentials(@Valid @RequestBody CreateCredentialRequest request) {
        User saved = createCredentialsUseCase.createCredentials(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getUserType(),
                request.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }
}
