package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.controller;

import co.edu.escuelaing.techcup.identity.domain.exception.InvalidTokenException;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.domain.port.in.AuthenticationUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.LogoutUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.OtpUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.PasswordRecoveryUseCase;
import co.edu.escuelaing.techcup.identity.domain.port.in.TokenValidationUseCase;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.GoogleLoginRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.LoginRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.OtpResendRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.OtpValidationRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.PasswordRecoveryRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.PasswordResetRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.MessageResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.OtpResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.TokenValidationResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Endpoints de autenticación, verificación OTP, recuperación de contraseña, validación de token JWT y cierre de sesión. " +
        "Cubre TC-06 (login institucional), TC-07 (login Google OAuth 2.0), TC-08 (validación JWT), " +
        "TC-09 (recuperación de contraseña), TC-10 (verificación OTP), TC-11 (expiración JWT) y TC-29 (logout).")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;
    private final LogoutUseCase logoutUseCase;
    private final OtpUseCase otpUseCase;
    private final PasswordRecoveryUseCase passwordRecoveryUseCase;
    private final TokenValidationUseCase tokenValidationUseCase;
    private final UserMapper userMapper;

    @PostMapping("/auth/login")
    @Operation(
            summary = "TC-06: Iniciar sesión con correo institucional y contraseña",
            description = "Autentica al usuario con su correo institucional (@escuelaing.edu.co) y contraseña. " +
                    "Si las credenciales son válidas, envía un código OTP al correo del usuario. " +
                    "El login NO está completo hasta que el OTP sea verificado mediante POST /otp/validate. " +
                    "Registra evento de auditoría USER_LOGIN. La cuenta se bloquea temporalmente (por defecto 15 minutos) " +
                    "tras alcanzar el máximo de intentos fallidos configurado (por defecto 5), y se desbloquea automáticamente " +
                    "al vencer ese periodo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales válidas. OTP enviado al correo del usuario."),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (email vacío, formato incorrecto, JSON malformado)."),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas (email o contraseña no coinciden)."),
            @ApiResponse(responseCode = "403", description = "Cuenta inactiva, o bloqueada temporalmente por múltiples intentos fallidos.")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UUID userId = authenticationUseCase.loginWithInstitutionalEmail(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(LoginResponse.builder()
                .userId(userId)
                .message("OTP sent to your email. Please validate to complete login.")
                .build());
    }

    @PostMapping("/auth/login/google")
    @Operation(
            summary = "TC-07: Iniciar sesión con Google OAuth 2.0",
            description = "Autentica al usuario mediante un token de Google OAuth 2.0 (obtenido con el flujo de consentimiento de Google). " +
                    "Pensado para invitados, árbitros, egresados sin correo institucional activo y organizadores. " +
                    "El usuario debe existir previamente en el sistema (creado vía el flujo de registro correspondiente); " +
                    "este endpoint no crea cuentas nuevas. Tras autenticación exitosa, se envía un OTP al correo. " +
                    "Registra evento de auditoría OTP_SENT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token de Google válido. OTP enviado al correo del usuario."),
            @ApiResponse(responseCode = "400", description = "Token de Google vacío o inválido, o dominio de correo no institucional."),
            @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o inactiva.")
    })
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        UUID userId = authenticationUseCase.loginWithGmail(request.getGoogleToken());
        return ResponseEntity.ok(LoginResponse.builder()
                .userId(userId)
                .message("OTP sent to your email. Please validate to complete login.")
                .build());
    }

    @PostMapping("/otp/validate")
    @Operation(
            summary = "TC-10: Verificar código OTP y obtener token JWT",
            description = "Valida el código OTP de 6 dígitos enviado al correo del usuario durante el login. " +
                    "Si el OTP es correcto y no ha expirado (configurable, por defecto 5 minutos), genera y retorna un token JWT " +
                    "junto con los datos del usuario autenticado. El OTP tiene un máximo de intentos permitidos (por defecto 3); " +
                    "si se exceden, el usuario debe solicitar un nuevo OTP. Este es el paso final del flujo de autenticación de dos factores."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP válido. Retorna JWT y datos del usuario autenticado."),
            @ApiResponse(responseCode = "400", description = "OTP incorrecto, expirado, o máximo de intentos alcanzado.")
    })
    public ResponseEntity<OtpResponse> validateOtp(@Valid @RequestBody OtpValidationRequest request) {
        String jwt = otpUseCase.validateOtp(request.getUserId(), request.getOtpCode());
        User user = tokenValidationUseCase.validateToken(jwt);
        return ResponseEntity.ok(OtpResponse.builder()
                .token(jwt)
                .user(userMapper.toResponse(user))
                .build());
    }

    @PostMapping("/otp/resend")
    @Operation(
            summary = "TC-10: Reenviar código OTP",
            description = "Genera y envía un nuevo código OTP al correo del usuario, invalidando cualquier OTP anterior. " +
                    "Tiene un cooldown configurable (por defecto 60 segundos) entre reenvíos para prevenir abuso. " +
                    "El nuevo OTP tiene la misma duración de expiración que el original."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nuevo OTP enviado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Cooldown activo o userId inválido."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    public ResponseEntity<MessageResponse> resendOtp(@Valid @RequestBody OtpResendRequest request) {
        otpUseCase.resendOtp(request.getUserId());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("OTP resent successfully")
                .build());
    }

    @PostMapping("/password/recovery")
    @Operation(
            summary = "TC-09: Solicitar código de recuperación de contraseña",
            description = "Envía un código de recuperación de un solo uso al correo institucional proporcionado. " +
                    "El código tiene un tiempo de expiración configurable (por defecto 15 minutos). " +
                    "Por seguridad, la respuesta siempre es 200 OK independientemente de si el correo existe en el sistema. " +
                    "Registra evento de auditoría PASSWORD_RECOVERY_REQUEST."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Si el correo existe, se envió el código de recuperación. Respuesta genérica por seguridad."),
            @ApiResponse(responseCode = "400", description = "Email vacío o con formato inválido.")
    })
    public ResponseEntity<MessageResponse> requestRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        passwordRecoveryUseCase.requestRecovery(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("If the email exists, a recovery code has been sent.")
                .build());
    }

    @PostMapping("/password/reset")
    @Operation(
            summary = "TC-09: Restablecer contraseña con código de recuperación",
            description = "Restablece la contraseña del usuario utilizando el código de recuperación recibido por correo. " +
                    "El código es de un solo uso y tiene tiempo de expiración (por defecto 15 minutos). " +
                    "La nueva contraseña se almacena con hash BCrypt. " +
                    "Registra evento de auditoría PASSWORD_RESET."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente."),
            @ApiResponse(responseCode = "400", description = "Código de recuperación inválido, expirado, o datos de entrada incompletos."),
            @ApiResponse(responseCode = "410", description = "Código de recuperación expirado (Gone).")
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordRecoveryUseCase.resetPassword(request.getEmail(), request.getRecoveryCode(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password reset successfully")
                .build());
    }

    @PostMapping("/token/validate")
    @Operation(
            summary = "TC-08: Validar token JWT",
            description = "Valida un token JWT enviado en el header Authorization (formato: 'Bearer <token>'). " +
                    "Verifica la firma, expiración absoluta, que no haya sido revocado (TC-29), y que la sesión no haya " +
                    "expirado por inactividad (TC-11: por defecto 30 minutos sin actividad). Cada llamada válida renueva " +
                    "la ventana de inactividad. Si es válido, retorna los datos del usuario: id, email y rol. " +
                    "Este endpoint es consumido por otros microservicios para verificar la autenticación del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido. Retorna datos del usuario autenticado."),
            @ApiResponse(responseCode = "400", description = "Header Authorization ausente."),
            @ApiResponse(responseCode = "401", description = "Token inválido, expirado, revocado, sesión expirada por inactividad, o header sin prefijo 'Bearer'.")
    })
    public ResponseEntity<TokenValidationResponse> validateToken(
            @Parameter(description = "Header de autorización con formato 'Bearer <JWT>'", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        User user = tokenValidationUseCase.validateToken(token);
        return ResponseEntity.ok(TokenValidationResponse.builder()
                .valid(true)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @PostMapping("/auth/logout")
    @Operation(
            summary = "TC-29: Cerrar sesión y revocar token JWT",
            description = "Cierra la sesión del usuario revocando su token JWT. El token se agrega a una lista de revocación en MongoDB " +
                    "con un índice TTL que lo elimina automáticamente al expirar. Cualquier intento de usar un token revocado " +
                    "será rechazado por el filtro de seguridad JWT. Registra evento de auditoría USER_LOGOUT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada y token revocado exitosamente."),
            @ApiResponse(responseCode = "401", description = "Header Authorization sin prefijo 'Bearer', token vacío, o token inválido.")
    })
    public ResponseEntity<MessageResponse> logout(
            @Parameter(description = "Header de autorización con formato 'Bearer <JWT>'", example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        logoutUseCase.logout(token);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Session closed successfully")
                .build());
    }

    private String extractBearerToken(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Authorization header must start with Bearer");
        }
        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            throw new InvalidTokenException("Bearer token must not be blank");
        }
        return token;
    }
}
