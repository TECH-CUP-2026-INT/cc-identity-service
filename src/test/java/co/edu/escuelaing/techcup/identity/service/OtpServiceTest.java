package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
import co.edu.escuelaing.techcup.identity.entity.OtpPurpose;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private JavaMailSender mailSender;

    private OtpService otpService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(otpCodeRepository, mailSender);

        ReflectionTestUtils.setField(
                otpService,
                "expirationMinutes",
                5
        );

        ReflectionTestUtils.setField(
                otpService,
                "otpLength",
                6
        );

        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashed-password")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .role(UserEntity.Role.USER)
                .build();
    }

    @Test
    void generateAndSend_deletesExpiredOtpAndSavesNewOtp() {
        otpService.generateAndSend(user);

        verify(otpCodeRepository).deleteByUserIdAndExpiresAtBefore(
                eq(user.getId()),
                any(LocalDateTime.class)
        );

        ArgumentCaptor<OtpCodeEntity> captor =
                ArgumentCaptor.forClass(OtpCodeEntity.class);

        verify(otpCodeRepository).save(captor.capture());

        OtpCodeEntity savedOtp = captor.getValue();

        assertNotNull(savedOtp.getId());
        assertEquals(user.getId(), savedOtp.getUserId());
        assertNotNull(savedOtp.getCode());
        assertEquals(6, savedOtp.getCode().length());
        assertFalse(savedOtp.isUsed());
        assertNotNull(savedOtp.getExpiresAt());

        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void verify_validOtp_marksOtpAsUsed() {
        OtpCodeEntity otp = OtpCodeEntity.builder()
                .id(UUID.randomUUID())
                .code("123456")
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
                        eq("123456"),
                        eq(user.getId()),
                        any(LocalDateTime.class)
                ))
                .thenReturn(Optional.of(otp));

        otpService.verify("123456", user);

        assertTrue(otp.isUsed());
        verify(otpCodeRepository).save(otp);
    }

    @Test
    void verify_invalidOtp_throwsException() {
        when(otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
                        eq("999999"),
                        eq(user.getId()),
                        any(LocalDateTime.class)
                ))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> otpService.verify("999999", user)
        );

        assertEquals("Invalid or expired OTP code", exception.getMessage());
        verify(otpCodeRepository, never()).save(any());
    }

    @Test
    void verifyWithPurpose_validOtp_marksOtpAsUsed() {
        OtpCodeEntity otp = OtpCodeEntity.builder()
                .id(UUID.randomUUID())
                .code("654321")
                .userId(user.getId())
                .purpose(OtpPurpose.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfterAndPurpose(
                        eq("654321"),
                        eq(user.getId()),
                        any(LocalDateTime.class),
                        eq(OtpPurpose.PASSWORD_RESET)
                ))
                .thenReturn(Optional.of(otp));

        otpService.verify(
                "654321",
                user,
                OtpPurpose.PASSWORD_RESET
        );

        assertTrue(otp.isUsed());
        verify(otpCodeRepository).save(otp);
    }

    @Test
    void verifyWithPurpose_invalidOtp_throwsException() {
        when(otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfterAndPurpose(
                        eq("000000"),
                        eq(user.getId()),
                        any(LocalDateTime.class),
                        eq(OtpPurpose.PASSWORD_RESET)
                ))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> otpService.verify(
                        "000000",
                        user,
                        OtpPurpose.PASSWORD_RESET
                )
        );

        assertEquals("Invalid or expired OTP code", exception.getMessage());
    }
}