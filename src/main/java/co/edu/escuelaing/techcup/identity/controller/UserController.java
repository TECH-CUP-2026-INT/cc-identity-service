package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.dto.ApiResponse;
import co.edu.escuelaing.techcup.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * SCRUM-61: Inhabilitar usuario.
 * Controlador REST para operaciones de gestión de usuarios.
 * Los endpoints de este controlador están restringidos a roles ADMIN y ORGANIZER.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * SCRUM-61: Inhabilitar usuario.
     * Deshabilita la cuenta de un usuario dado su UUID.
     * Solo los roles ADMIN y ORGANIZER pueden ejecutar esta acción.
     *
     * @param userId UUID del usuario a inhabilitar
     * @return ApiResponse con mensaje de confirmación
     */
    @Operation(summary = "Disable a user account")
    @PatchMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse> disableUser(@PathVariable UUID userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok(new ApiResponse("User disabled successfully", true));
    }
}