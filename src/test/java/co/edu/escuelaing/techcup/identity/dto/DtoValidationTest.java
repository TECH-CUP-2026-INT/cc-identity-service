package co.edu.escuelaing.techcup.identity.dto;

import co.edu.escuelaing.techcup.identity.entity.UserRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    @Test
    void registerRequest_AllFields_Work() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@email.com");
        request.setPassword("pass");
        request.setName("Name");
        request.setRole(UserRole.STUDENT);
        request.setAssociatedStudentId("123");
        request.setAcademicProgram("Engineering");
        request.setSemester(5);

        assertEquals("test@email.com", request.getEmail());
        assertEquals("pass", request.getPassword());
        assertEquals("Name", request.getName());
        assertEquals(UserRole.STUDENT, request.getRole());
        assertEquals("123", request.getAssociatedStudentId());
        assertEquals("Engineering", request.getAcademicProgram());
        assertEquals(5, request.getSemester());
    }

    @Test
    void loginRequest_AllFields_Work() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@email.com");
        request.setPassword("pass");

        assertEquals("test@email.com", request.getEmail());
        assertEquals("pass", request.getPassword());
    }

    @Test
    void authResponse_AllFields_Work() {
        AuthResponse response = new AuthResponse("access", "refresh");

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void refereeRequest_AllFields_Work() {
        RefereeRequestDTO request = new RefereeRequestDTO();
        request.setEmail("referee@email.com");
        request.setName("Referee");

        assertEquals("referee@email.com", request.getEmail());
        assertEquals("Referee", request.getName());
    }

    @Test
    void otpVerifyRequest_AllFields_Work() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setEmail("test@email.com");
        request.setOtp("123456");

        assertEquals("test@email.com", request.getEmail());
        assertEquals("123456", request.getOtp());
    }

    @Test
    void refreshTokenRequest_AllFields_Work() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        assertEquals("refresh-token", request.getRefreshToken());
    }
}