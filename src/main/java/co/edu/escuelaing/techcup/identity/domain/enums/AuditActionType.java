package co.edu.escuelaing.techcup.identity.domain.enums;

public enum AuditActionType {
    USER_REGISTERED,
    USER_LOGIN,
    USER_LOGIN_FAILED,
    OTP_SENT,
    OTP_VALIDATED,
    OTP_FAILED,
    PASSWORD_RECOVERY_REQUESTED,
    PASSWORD_RESET,
    SESSION_EXPIRED,
    ACCOUNT_DISABLED,
    USER_LOGOUT,
    CREDENTIALS_CREATED
}
