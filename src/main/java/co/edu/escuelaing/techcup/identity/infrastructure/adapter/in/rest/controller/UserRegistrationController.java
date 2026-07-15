package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.RegisterUserUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateAdminOrganizerRequest;
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

@RestController
@RequestMapping("/api/v1/register")
@Tag(name = "Registration", description = "Endpoint de creación de cuentas de administrador y organizador gestionado directamente por el Identity Service. " +
        "Cubre el requisito funcional TC-05. Los registros de estudiantes, invitados y egresados se gestionan a través del " +
        "users-players-service, que invoca el endpoint interno POST /api/v1/internal/credentials para crear las credenciales.")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserMapper userMapper;

    @PostMapping("/admin-organizer")
    @Operation(
            summary = "TC-05: Crear cuenta de administrador u organizador",
            description = "Registra una nueva cuenta de tipo ADMIN u ORGANIZER en la plataforma TechCup. " +
                    "Requiere correo institucional (@escuelaing.edu.co), contraseña y tipo de usuario. " +
                    "La contraseña se almacena con hash BCrypt. La cuenta se crea en estado ACTIVE. " +
                    "Si el correo ya está registrado, retorna 409 Conflict. " +
                    "Registra evento de auditoría USER_REGISTERED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente. Retorna los datos del usuario registrado."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos: email vacío/formato incorrecto, contraseña vacía, dominio no institucional, o tipo de usuario no permitido (solo ADMIN/ORGANIZER)."),
            @ApiResponse(responseCode = "409", description = "Ya existe una cuenta con el correo proporcionado.")
    })
    public ResponseEntity<UserResponse> createAdminOrOrganizer(@Valid @RequestBody CreateAdminOrganizerRequest request) {
        User user = userMapper.toDomain(request);
        User saved = registerUserUseCase.createAdminOrOrganizer(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }
}
