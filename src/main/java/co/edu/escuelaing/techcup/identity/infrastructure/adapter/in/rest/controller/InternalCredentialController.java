package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.CreateCredentialsUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateCredentialRequest;
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

/**
 * Internal endpoint consumed by users-players-service for credential creation
 * during student, guest, and graduate registration (TC-01, TC-02, TC-03).
 */
@RestController
@RequestMapping("/api/v1/internal/credentials")
@Tag(name = "Internal", description = "Inter-service credential management")
@RequiredArgsConstructor
public class InternalCredentialController {

    private final CreateCredentialsUseCase createCredentialsUseCase;
    private final UserMapper userMapper;

    @PostMapping
    @Operation(summary = "Create credentials for a user registered in users-players-service")
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
