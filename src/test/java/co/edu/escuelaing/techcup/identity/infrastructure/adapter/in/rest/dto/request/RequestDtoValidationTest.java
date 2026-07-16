package co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request;

import co.edu.escuelaing.techcup.identity.domain.enums.UserRole;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void loginRequestRejectsNullBlankAndInvalidEmailValues() {
        LoginRequest request = LoginRequest.builder()
                .email("bad-email")
                .password("   ")
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "password");
    }

    @Test
    void loginRequestAcceptsValidPayload() {
        LoginRequest request = LoginRequest.builder()
                .email("student@escuelaing.edu.co")
                .password("Password123!")
                .build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void googleLoginRequestRejectsBlankToken() {
        GoogleLoginRequest request = GoogleLoginRequest.builder()
                .googleToken("   ")
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("googleToken");
    }

    @Test
    void otpValidationRequestRejectsMissingUserIdAndOtpCode() {
        OtpValidationRequest request = OtpValidationRequest.builder()
                .userId(null)
                .otpCode("")
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("userId", "otpCode");
    }

    @Test
    void otpResendRequestRejectsNullUserId() {
        OtpResendRequest request = OtpResendRequest.builder()
                .userId(null)
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("userId");
    }

    @Test
    void passwordRecoveryRequestRejectsInvalidEmail() {
        PasswordRecoveryRequest request = PasswordRecoveryRequest.builder()
                .email("missing-at-symbol")
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    void passwordResetRequestRejectsInvalidEmailBlankCodeAndBlankPassword() {
        PasswordResetRequest request = PasswordResetRequest.builder()
                .email("invalid")
                .recoveryCode("")
                .newPassword("   ")
                .build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "recoveryCode", "newPassword");
    }

    @Test
    void createCredentialRequestRejectsEveryMissingRequiredField() {
        CreateCredentialRequest request = CreateCredentialRequest.builder().build();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("userId", "email", "password", "fullName", "userType", "role");
    }

    @Test
    void createCredentialRequestAcceptsStudentCredentialPayload() {
        CreateCredentialRequest request = CreateCredentialRequest.builder()
                .userId(java.util.UUID.randomUUID())
                .email("student@escuelaing.edu.co")
                .password("Password123!")
                .fullName("Ada Lovelace")
                .userType(UserType.STUDENT)
                .role(UserRole.PLAYER)
                .build();

        assertThat(validator.validate(request)).isEmpty();
    }

}
