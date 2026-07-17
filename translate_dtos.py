import os, re

translations = {
    'Respuesta gen\u00e9rica con mensaje de confirmaci\u00f3n': 'Generic response with confirmation message',
    'Mensaje descriptivo del resultado de la operaci\u00f3n': 'Descriptive message of the operation result',
    'Datos del usuario registrado en la plataforma TechCup': 'User data registered in the TechCup platform',
    'Los campos opcionales dependen del tipo de usuario.': 'Optional fields depend on user type.',
    'ID \u00fanico del usuario (fuente de verdad: users-players-service)': 'Unique user ID (source of truth: users-players-service)',
    'Nombre completo del usuario': 'User full name',
    'Correo institucional del usuario': 'User institutional email',
    'Tipo de usuario seg\u00fan su relaci\u00f3n con la instituci\u00f3n': 'User type based on their relationship with the institution',
    'Rol del usuario en la plataforma TechCup': 'User role in the TechCup platform',
    'Estado de la cuenta (ACTIVE, INACTIVE, BLOCKED)': 'Account status (ACTIVE, INACTIVE, BLOCKED)',
    'Tipo de documento de identidad (solo estudiantes/egresados)': 'ID document type (students/alumni only)',
    'N\u00famero de documento de identidad': 'ID document number',
    'Fecha de nacimiento del usuario': 'User birth date',
    'Programa acad\u00e9mico actual (solo estudiantes)': 'Current academic program (students only)',
    'Semestre actual (solo estudiantes)': 'Current semester (students only)',
    'ID del estudiante asociado (solo invitados/guests)': 'Associated student ID (guests only)',
    'Relaci\u00f3n con el estudiante asociado (solo invitados)': 'Relationship with associated student (guests only)',
    'Programa acad\u00e9mico del que se gradu\u00f3 (solo egresados)': 'Graduated academic program (alumni only)',
    'Fecha y hora de creaci\u00f3n de la cuenta': 'Account creation timestamp',
    'Fecha y hora de la \u00faltima actualizaci\u00f3n': 'Last update timestamp',
    'Evento de auditor\u00eda de seguridad registrado por el Identity Service': 'Security audit event recorded by Identity Service',
    'ID \u00fanico del evento de auditor\u00eda': 'Unique audit event ID',
    'ID del usuario que gener\u00f3 el evento': 'ID of the user who triggered the event',
    'Tipo de acci\u00f3n registrada': 'Recorded action type',
    'USER_LOGIN, USER_LOGOUT, PASSWORD_RESET, etc.': 'USER_LOGIN, USER_LOGOUT, PASSWORD_RESET, etc.',
    'Descripci\u00f3n legible del evento': 'Human-readable event description',
    'Indica si la acci\u00f3n fue exitosa o fallida': 'Indicates whether the action was successful',
    'Fecha y hora en que ocurri\u00f3 el evento': 'Event timestamp',
    'Respuesta de login exitoso. El login NO est\u00e1 completo': 'Successful login response. Login is NOT complete',
    'se requiere verificaci\u00f3n OTP.': 'OTP verification is required.',
    'ID del usuario autenticado. Necesario para el paso de verificaci\u00f3n OTP.': 'Authenticated user ID. Required for the OTP verification step.',
    'Mensaje indicando el siguiente paso del flujo de autenticaci\u00f3n': 'Message indicating the next step in the authentication flow',
    'Respuesta de validaci\u00f3n de token JWT': 'JWT token validation response',
    'Usado por otros microservicios para verificar autenticaci\u00f3n.': 'Used by other microservices to verify authentication.',
    'Indica si el token es v\u00e1lido': 'Indicates whether the token is valid',
    'firma correcta, no expirado, no revocado': 'correct signature, not expired, not revoked',
    'ID del usuario propietario del token': 'ID of the token owner user',
    'Rol del usuario en la plataforma': 'User role in the platform',
    'ADMIN, ORGANIZER, PLAYER, VIEWER': 'ADMIN, ORGANIZER, PLAYER, VIEWER',
    'Correo electr\u00f3nico asociado a un userId.': 'Email associated with a userId.',
    'Usado por am-notification-service para resolver el destinatario real de una notificaci\u00f3n.': 'Used by am-notification-service to resolve the actual notification recipient.',
    'Correo del usuario': 'User email',
    'Respuesta de verificaci\u00f3n OTP exitosa.': 'Successful OTP verification response.',
    'Contiene el JWT y datos del usuario autenticado.': 'Contains JWT and authenticated user data.',
    'Token JWT para autenticaci\u00f3n en requests posteriores.': 'JWT token for authentication in subsequent requests.',
    'Incluir como': 'Include as',
    'Bearer <token> en el header Authorization.': 'Bearer <token> in the Authorization header.',
    'Datos del usuario autenticado': 'Authenticated user data',
    'DTO interno para actualizaci\u00f3n de estado de cuenta desde users-players-service.': 'Internal DTO for account status update from users-players-service.',
    'Nuevo estado de la cuenta': 'New account status',
    'Solicitud de recuperaci\u00f3n de contrase\u00f1a.': 'Password recovery request.',
    'Env\u00eda un c\u00f3digo de un solo uso al correo.': 'Sends a single-use code to the email.',
    'Correo institucional asociado a la cuenta': 'Institutional email associated with the account',
    'Solicitud de verificaci\u00f3n del c\u00f3digo OTP': 'OTP code verification request',
    'para completar el flujo de autenticaci\u00f3n de dos factores': 'to complete the two-factor authentication flow',
    'ID del usuario que recibi\u00f3 el OTP durante el login': 'ID of the user who received the OTP during login',
    'C\u00f3digo OTP de 6 d\u00edgitos enviado al correo del usuario': '6-digit OTP code sent to the user email',
    'Solicitud de reenv\u00edo de c\u00f3digo OTP': 'OTP code resend request',
    'ID del usuario al que se le reenviar\u00e1 el OTP': 'ID of the user to resend the OTP to',
    'Solicitud de restablecimiento de contrase\u00f1a': 'Password reset request',
    'usando el c\u00f3digo de recuperaci\u00f3n recibido por correo': 'using the recovery code received by email',
    'C\u00f3digo de recuperaci\u00f3n de un solo uso recibido por correo': 'Single-use recovery code received by email',
    'expira en 15 minutos por defecto': 'expires in 15 minutes by default',
    'Nueva contrase\u00f1a para la cuenta': 'New password for the account',
    'Solicitud de inicio de sesi\u00f3n con Google OAuth 2.0': 'Google OAuth 2.0 login request',
    'Token ID obtenido del flujo de consentimiento de Google OAuth 2.0': 'ID token obtained from the Google OAuth 2.0 consent flow',
    'DTO interno para creaci\u00f3n de credenciales desde users-players-service.': 'Internal DTO for credential creation from users-players-service.',
    'ID del usuario generado por users-players-service (fuente de verdad)': 'User ID generated by users-players-service (source of truth)',
    'Correo del usuario (institucional o Gmail seg\u00fan tipo)': 'User email (institutional or Gmail depending on type)',
    'Contrase\u00f1a en texto plano (se almacenar\u00e1 con hash BCrypt)': 'Plain text password (will be stored with BCrypt hash)',
    'Solicitud de inicio de sesi\u00f3n con correo institucional y contrase\u00f1a': 'Login request with institutional email and password',
    'Correo institucional del usuario (@escuelaing.edu.co)': 'User institutional email (@escuelaing.edu.co)',
    'Contrase\u00f1a del usuario': 'User password',
    'DTO interno para actualizaci\u00f3n de rol desde users-players-service o teams-service.': 'Internal DTO for role update from users-players-service or teams-service.',
    'Nuevo rol del usuario': 'New user role',
}

# Remove empties
translations = {k: v for k, v in translations.items() if k.strip()}

base = r'C:\Users\RANGE\Downloads\cc-identity-service\src\main\java\co\edu\escuelaing\techcup\identity\infrastructure\adapter\in\rest\dto'

for root, dirs, files in os.walk(base):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as fh:
                content = fh.read()
            for es, en in translations.items():
                content = content.replace(es, en)
            with open(path, 'w', encoding='utf-8') as fh:
                fh.write(content)

print('DTO translations done')
