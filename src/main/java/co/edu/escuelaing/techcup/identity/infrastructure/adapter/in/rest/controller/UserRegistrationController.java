package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.RegisterUserUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateAdminOrganizerRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Registration", description = "Account creation endpoint managed by Identity Service (TC-05)")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserMapper userMapper;

    @PostMapping("/admin-organizer")
    @Operation(summary = "TC-05: Create admin or organizer account (self-registration)")
    public ResponseEntity<UserResponse> createAdminOrOrganizer(@Valid @RequestBody CreateAdminOrganizerRequest request) {
        User user = userMapper.toDomain(request);
        User saved = registerUserUseCase.createAdminOrOrganizer(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }
}
