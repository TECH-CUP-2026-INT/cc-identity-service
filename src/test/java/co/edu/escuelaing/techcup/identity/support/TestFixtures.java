package co.edu.escuelaing.techcup.identity.support;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.IdType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.OtpTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RecoveryTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RevokedTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

public final class TestFixtures {

    public static final String USER_ID = "user-1";
    public static final String EMAIL = "student@escuelaing.edu.co";
    public static final String PASSWORD = "Password123!";
    public static final String ENCODED_PASSWORD = "encoded-password";
    public static final String OTP_CODE = "123456";
    public static final String JWT = "jwt-token";

    private TestFixtures() {
    }

    public static User activeUser() {
        return User.builder()
                .id(USER_ID)
                .fullName("Ada Lovelace")
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .userType(UserType.STUDENT)
                .role(UserRole.PLAYER)
                .status(AccountStatus.ACTIVE)
                .idType(IdType.CC)
                .idNumber("123456789")
                .dateOfBirth(LocalDate.of(2000, Month.JANUARY, 1))
                .academicProgram("Computer Science")
                .semester(5)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    public static User inactiveUser() {
        User user = activeUser();
        user.setStatus(AccountStatus.INACTIVE);
        return user;
    }

    public static User adminUser() {
        User user = activeUser();
        user.setUserType(UserType.ADMIN);
        user.setRole(UserRole.ADMIN);
        user.setEmail("admin@escuelaing.edu.co");
        return user;
    }

    public static User organizerWithoutRole() {
        return User.builder()
                .fullName("Grace Hopper")
                .email("organizer@escuelaing.edu.co")
                .password(PASSWORD)
                .userType(UserType.ORGANIZER)
                .build();
    }

    public static OtpToken validOtp() {
        return OtpToken.builder()
                .id("otp-1")
                .userId(USER_ID)
                .code(OTP_CODE)
                .failedAttempts(0)
                .used(false)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .build();
    }

    public static OtpToken expiredOtp() {
        OtpToken token = validOtp();
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        return token;
    }

    public static RecoveryToken validRecoveryToken() {
        return RecoveryToken.builder()
                .id("recovery-1")
                .userId(USER_ID)
                .code("ABCD1234")
                .used(false)
                .createdAt(LocalDateTime.now().minusMinutes(1))
                .expiresAt(LocalDateTime.now().plusMinutes(14))
                .build();
    }

    public static RecoveryToken expiredRecoveryToken() {
        RecoveryToken token = validRecoveryToken();
        token.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        return token;
    }

    public static RevokedToken revokedToken() {
        return RevokedToken.builder()
                .id("revoked-1")
                .token(JWT)
                .userId(USER_ID)
                .revokedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    public static AuditEvent auditEvent() {
        return AuditEvent.builder()
                .id("audit-1")
                .userId(USER_ID)
                .actionType(AuditActionType.USER_LOGIN)
                .description("Successful login")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static UserResponse userResponse() {
        User user = activeUser();
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .userType(user.getUserType())
                .role(user.getRole())
                .status(user.getStatus())
                .idType(user.getIdType())
                .idNumber(user.getIdNumber())
                .dateOfBirth(user.getDateOfBirth())
                .academicProgram(user.getAcademicProgram())
                .semester(user.getSemester())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static AuditEventResponse auditEventResponse() {
        AuditEvent event = auditEvent();
        return AuditEventResponse.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .actionType(event.getActionType())
                .description(event.getDescription())
                .success(event.isSuccess())
                .timestamp(event.getTimestamp())
                .build();
    }

    public static UserDocument userDocument() {
        User user = activeUser();
        return UserDocument.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .userType(user.getUserType())
                .role(user.getRole())
                .status(user.getStatus())
                .idType(user.getIdType())
                .idNumber(user.getIdNumber())
                .dateOfBirth(user.getDateOfBirth())
                .academicProgram(user.getAcademicProgram())
                .semester(user.getSemester())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static OtpTokenDocument otpTokenDocument() {
        OtpToken token = validOtp();
        return OtpTokenDocument.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .code(token.getCode())
                .failedAttempts(token.getFailedAttempts())
                .used(token.isUsed())
                .createdAt(token.getCreatedAt())
                .expiresAt(token.getExpiresAt())
                .build();
    }

    public static RecoveryTokenDocument recoveryTokenDocument() {
        RecoveryToken token = validRecoveryToken();
        return RecoveryTokenDocument.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .code(token.getCode())
                .used(token.isUsed())
                .createdAt(token.getCreatedAt())
                .expiresAt(token.getExpiresAt())
                .build();
    }

    public static RevokedTokenDocument revokedTokenDocument() {
        RevokedToken token = revokedToken();
        return RevokedTokenDocument.builder()
                .id(token.getId())
                .token(token.getToken())
                .userId(token.getUserId())
                .revokedAt(token.getRevokedAt())
                .expiresAt(token.getExpiresAt())
                .build();
    }

    public static AuditEventDocument auditEventDocument() {
        AuditEvent event = auditEvent();
        return AuditEventDocument.builder()
                .id(event.getId())
                .userId(event.getUserId())
                .actionType(event.getActionType())
                .description(event.getDescription())
                .success(event.isSuccess())
                .timestamp(event.getTimestamp())
                .build();
    }
}
