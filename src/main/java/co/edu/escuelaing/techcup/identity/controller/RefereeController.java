package co.edu.escuelaing.techcup.identity.controller;

import co.edu.escuelaing.techcup.identity.dto.RefereeRequestDTO;
import co.edu.escuelaing.techcup.identity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * SCRUM-22: Controlador para registro de arbitros.
 * Solo accesible para usuarios con rol ORGANIZER.
 */
@RestController
@RequestMapping("/api/v1/referees")
public class RefereeController {

    private final UserService userService;

    public RefereeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * SCRUM-22: Crea un nuevo arbitro en el sistema.
     * Genera contrasena temporal y envia credenciales por email.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ORGANIZER')")
    public void createReferee(@Valid @RequestBody RefereeRequestDTO dto) {
        userService.createReferee(dto);
    }
}
