package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
import co.edu.escuelaing.techcup.identity.entity.OtpPurpose;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.OtpCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Service responsible for OTP generation, email delivery, and verification.
 * Implements SCRUM-13: double factor OTP validation.
 */
@Service
public class OtpService {

    @Value("${app.otp.expiration-minutes}")
    private int expirationMinutes;

    @Value("${app.otp.length}")
    private int otpLength;

    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;

    public OtpService(OtpCodeRepository otpCodeRepository, JavaMailSender mailSender) {
        this.otpCodeRepository = otpCodeRepository;
        this.mailSender = mailSender;
    }

    /**
     * Backward-compatible overload — uses REGISTRATION purpose.
     * Keeps existing callers (AuthService, UserService) unchanged.
     */
    @Transactional
    public void generateAndSend(UserEntity user) {
        generateAndSend(user, OtpPurpose.REGISTRATION);
    }

    /**
     * Generates a numeric OTP with a specific purpose, saves it and sends it via email.
     */
    @Transactional
    public void generateAndSend(UserEntity user, OtpPurpose purpose) {
        otpCodeRepository.deleteByUserIdAndExpiresAtBefore(
                user.getId(),
                LocalDateTime.now()
        );

        String code = generateCode();

        OtpCodeEntity otp = OtpCodeEntity.builder()
                .code(code)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .purpose(purpose)
                .build();

        otpCodeRepository.save(otp);
        sendEmail(user.getEmail(), code);
    }

    /**
     * Backward-compatible overload — verifies without purpose filter.
     */
    @Transactional
    public void verify(String code, UserEntity user) {
        OtpCodeEntity otp = otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(code,user.getId(),LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP code"));

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }

    /**
     * Verifies the OTP code for the given user and purpose.
     */
    @Transactional
    public void verify(String code, UserEntity user, OtpPurpose purpose) {
        OtpCodeEntity otp = otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfterAndPurpose(code, user.getId(), LocalDateTime.now(), purpose)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP code"));

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + code + "\nThis code will expire in " + expirationMinutes + " minutes.");
        mailSender.send(message);
    }
}
