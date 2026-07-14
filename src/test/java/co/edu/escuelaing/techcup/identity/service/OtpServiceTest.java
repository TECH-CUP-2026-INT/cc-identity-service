package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.OtpCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private OtpService otpService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "expirationMinutes", 5);
        ReflectionTestUtils.setField(otpService, "otpLength", 6);

        user = UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void generateAndSend_savesOtpAndSendsEmail() {
        otpService.generateAndSend(user);

        verify(otpCodeRepository).deleteByUserIdAndExpiresAtBefore(eq(user.getId()), any(LocalDateTime.class));
        verify(otpCodeRepository).save(any(OtpCodeEntity.class));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void generateAndSend_otpCodeHasCorrectLength() {
        otpService.generateAndSend(user);

        verify(otpCodeRepository).save(argThat(otp ->
                otp.getCode().length() == 6 && otp.getCode().matches("\\d{6}")
        ));
    }

    @Test
    void verify_validOtp_marksAsUsed() {
        OtpCodeEntity otpCode = OtpCodeEntity.builder()
                .code("123456")
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(otpCodeRepository.findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
                eq("123456"), eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(otpCode));

        otpService.verify("123456", user);

        assertTrue(otpCode.isUsed());
        verify(otpCodeRepository).save(otpCode);
    }

    @Test
    void verify_invalidOtp_throwsException() {
        when(otpCodeRepository.findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
                eq("000000"), eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> otpService.verify("000000", user));
        verify(otpCodeRepository, never()).save(any());
    }

    @Test
    void verify_expiredOtp_throwsException() {
        when(otpCodeRepository.findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(
                eq("123456"), eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> otpService.verify("123456", user));
    }
}
