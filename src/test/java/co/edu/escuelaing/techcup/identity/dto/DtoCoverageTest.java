package co.edu.escuelaing.techcup.identity.dto;

import co.edu.escuelaing.techcup.identity.entity.UserRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    // ==================== REGISTER REQUEST ====================
    @Test
    void testRegisterRequest_AllGettersAndSetters() {
        RegisterRequest request = new RegisterRequest();
        
        // Set all values
        request.setEmail("test@email.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setRole(UserRole.STUDENT);
        request.setAssociatedStudentId("student-123");
        request.setAcademicProgram("Engineering");
        request.setSemester(5);
        
        // Get all values
        assertEquals("test@email.com", request.getEmail());
        assertEquals("password", request.getPassword());
        assertEquals("Test User", request.getName());
        assertEquals(UserRole.STUDENT, request.getRole());
        assertEquals("student-123", request.getAssociatedStudentId());
        assertEquals("Engineering", request.getAcademicProgram());
        assertEquals(5, request.getSemester());
        
        // Change all values
        request.setEmail("new@email.com");
        request.setPassword("newpassword");
        request.setName("New User");
        request.setRole(UserRole.ADMIN);
        request.setAssociatedStudentId("new-student");
        request.setAcademicProgram("Math");
        request.setSemester(3);
        
        assertEquals("new@email.com", request.getEmail());
        assertEquals("newpassword", request.getPassword());
        assertEquals("New User", request.getName());
        assertEquals(UserRole.ADMIN, request.getRole());
        assertEquals("new-student", request.getAssociatedStudentId());
        assertEquals("Math", request.getAcademicProgram());
        assertEquals(3, request.getSemester());
        
        // Test null values
        request.setEmail(null);
        request.setPassword(null);
        request.setName(null);
        request.setRole(null);
        request.setAssociatedStudentId(null);
        request.setAcademicProgram(null);
        request.setSemester(null);
        
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getName());
        assertNull(request.getRole());
        assertNull(request.getAssociatedStudentId());
        assertNull(request.getAcademicProgram());
        assertNull(request.getSemester());
    }

    // ==================== LOGIN REQUEST ====================
    @Test
    void testLoginRequest_AllGettersAndSetters() {
        LoginRequest request = new LoginRequest();
        
        request.setEmail("test@email.com");
        request.setPassword("password");
        
        assertEquals("test@email.com", request.getEmail());
        assertEquals("password", request.getPassword());
        
        request.setEmail("new@email.com");
        request.setPassword("newpassword");
        
        assertEquals("new@email.com", request.getEmail());
        assertEquals("newpassword", request.getPassword());
        
        request.setEmail(null);
        request.setPassword(null);
        
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    // ==================== AUTH RESPONSE ====================
    @Test
    void testAuthResponse_AllGettersAndSetters() {
        AuthResponse response = new AuthResponse("access", "refresh");
        
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        
        response.setAccessToken("newaccess");
        response.setRefreshToken("newrefresh");
        
        assertEquals("newaccess", response.getAccessToken());
        assertEquals("newrefresh", response.getRefreshToken());
        
        response.setAccessToken(null);
        response.setRefreshToken(null);
        
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
    }

    // ==================== REFEREE REQUEST ====================
    @Test
    void testRefereeRequestDTO_AllGettersAndSetters() {
        RefereeRequestDTO request = new RefereeRequestDTO();
        
        request.setEmail("referee@email.com");
        request.setName("Referee User");
        
        assertEquals("referee@email.com", request.getEmail());
        assertEquals("Referee User", request.getName());
        
        request.setEmail("new@email.com");
        request.setName("New Referee");
        
        assertEquals("new@email.com", request.getEmail());
        assertEquals("New Referee", request.getName());
        
        request.setEmail(null);
        request.setName(null);
        
        assertNull(request.getEmail());
        assertNull(request.getName());
    }

    // ==================== OTP VERIFY REQUEST ====================
    @Test
    void testOtpVerifyRequest_AllGettersAndSetters() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        
        request.setEmail("test@email.com");
        request.setOtp("123456");
        
        assertEquals("test@email.com", request.getEmail());
        assertEquals("123456", request.getOtp());
        
        request.setEmail("new@email.com");
        request.setOtp("654321");
        
        assertEquals("new@email.com", request.getEmail());
        assertEquals("654321", request.getOtp());
        
        request.setEmail(null);
        request.setOtp(null);
        
        assertNull(request.getEmail());
        assertNull(request.getOtp());
    }

    // ==================== REFRESH TOKEN REQUEST ====================
    @Test
    void testRefreshTokenRequest_AllGettersAndSetters() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        
        request.setRefreshToken("refresh-token");
        
        assertEquals("refresh-token", request.getRefreshToken());
        
        request.setRefreshToken("new-refresh-token");
        
        assertEquals("new-refresh-token", request.getRefreshToken());
        
        request.setRefreshToken(null);
        
        assertNull(request.getRefreshToken());
    }
}