package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.enums.AuditActionType;
import co.edu.escuelaing.techcup.identity.domain.enums.UserType;
import co.edu.escuelaing.techcup.identity.domain.model.AuditEvent;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.RecoveryToken;
import co.edu.escuelaing.techcup.identity.domain.model.RevokedToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.request.CreateAdminOrganizerRequest;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.in.rest.dto.response.UserResponse;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.AuditEventDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.OtpTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RecoveryTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.RevokedTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final AuditEventMapper auditEventMapper = Mappers.getMapper(AuditEventMapper.class);
    private final OtpTokenMapper otpTokenMapper = Mappers.getMapper(OtpTokenMapper.class);
    private final RecoveryTokenMapper recoveryTokenMapper = Mappers.getMapper(RecoveryTokenMapper.class);
    private final RevokedTokenMapper revokedTokenMapper = Mappers.getMapper(RevokedTokenMapper.class);

    @Test
    void userMapperMapsDomainDocumentResponseAndAdminOrganizerRequest() {
        User user = TestFixtures.activeUser();

        UserDocument document = userMapper.toDocument(user);
        User roundTrip = userMapper.toDomain(document);
        UserResponse response = userMapper.toResponse(user);
        User fromRequest = userMapper.toDomain(CreateAdminOrganizerRequest.builder()
                .fullName("Grace Hopper")
                .email("organizer@escuelaing.edu.co")
                .password("Password123!")
                .userType(UserType.ORGANIZER)
                .build());

        assertThat(document.getEmail()).isEqualTo(user.getEmail());
        assertThat(roundTrip.getId()).isEqualTo(user.getId());
        assertThat(roundTrip.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(fromRequest.getId()).isNull();
        assertThat(fromRequest.getFullName()).isEqualTo("Grace Hopper");
        assertThat(fromRequest.getRole()).isNull();
    }

    @Test
    void auditEventMapperMapsDomainDocumentAndResponse() {
        AuditEvent event = TestFixtures.auditEvent();

        AuditEventDocument document = auditEventMapper.toDocument(event);
        AuditEvent roundTrip = auditEventMapper.toDomain(document);
        var response = auditEventMapper.toResponse(event);

        assertThat(document.getActionType()).isEqualTo(AuditActionType.USER_LOGIN);
        assertThat(roundTrip.getUserId()).isEqualTo(event.getUserId());
        assertThat(response.getDescription()).isEqualTo(event.getDescription());
    }

    @Test
    void tokenMappersMapDomainAndDocuments() {
        OtpToken otp = TestFixtures.validOtp();
        OtpTokenDocument otpDocument = otpTokenMapper.toDocument(otp);
        assertThat(otpTokenMapper.toDomain(otpDocument).getCode()).isEqualTo(otp.getCode());

        RecoveryToken recoveryToken = TestFixtures.validRecoveryToken();
        RecoveryTokenDocument recoveryDocument = recoveryTokenMapper.toDocument(recoveryToken);
        assertThat(recoveryTokenMapper.toDomain(recoveryDocument).getCode()).isEqualTo(recoveryToken.getCode());

        RevokedToken revokedToken = TestFixtures.revokedToken();
        RevokedTokenDocument revokedDocument = revokedTokenMapper.toDocument(revokedToken);
        assertThat(revokedTokenMapper.toDomain(revokedDocument).getToken()).isEqualTo(revokedToken.getToken());
    }
}
