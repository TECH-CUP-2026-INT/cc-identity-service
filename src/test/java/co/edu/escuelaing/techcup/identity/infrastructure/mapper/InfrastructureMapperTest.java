package co.edu.escuelaing.techcup.identity.infrastructure.mapper;

import co.edu.escuelaing.techcup.identity.domain.enums.AccountStatus;
import co.edu.escuelaing.techcup.identity.domain.model.OtpToken;
import co.edu.escuelaing.techcup.identity.domain.model.User;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.OtpTokenDocument;
import co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.persistence.document.UserDocument;
import co.edu.escuelaing.techcup.identity.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class InfrastructureMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final OtpTokenMapper otpTokenMapper = Mappers.getMapper(OtpTokenMapper.class);

    @Test
    void userMapperMapsDomainDocumentAndResponse() {
        User user = TestFixtures.activeUser();

        UserDocument document = userMapper.toDocument(user);
        User roundTrip = userMapper.toDomain(document);
        var response = userMapper.toResponse(user);

        assertThat(document.getEmail()).isEqualTo(user.getEmail());
        assertThat(roundTrip.getId()).isEqualTo(user.getId());
        assertThat(roundTrip.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void otpTokenMapperMapsDomainAndDocument() {
        OtpToken otp = TestFixtures.validOtp();
        OtpTokenDocument otpDocument = otpTokenMapper.toDocument(otp);
        assertThat(otpTokenMapper.toDomain(otpDocument).getCode()).isEqualTo(otp.getCode());
    }
}
