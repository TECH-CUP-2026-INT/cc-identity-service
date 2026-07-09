package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
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
     * Generates a numeric OTP, saves it to the database, and sends it via email.
     * Deletes any previous expired OTPs for the user before creating a new one.
     * @param user the user who requested the OTP
     */
    @Transactional
    public void generateAndSend(UserEntity user) {
        otpCodeRepository.deleteExpiredByUser(user, LocalDateTime.now());

        String code = generateCode();

        OtpCodeEntity otp = OtpCodeEntity.builder()
                .code(code)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build();

        otpCodeRepository.save(otp);
        sendEmail(user.getEmail(), code);
    }
    /**
     * Verifies the OTP code for the given user.
     * Marks the OTP as used if valid.
     * @param code the OTP code entered by the user
     * @param user the user attempting verification
     * @throws RuntimeException if OTP is invalid or expired
     */
    
    @Transactional
    public void verify(String code, UserEntity user) {
        OtpCodeEntity otp = otpCodeRepository
                .findByCodeAndUserAndUsedFalseAndExpiresAtAfter(code, user, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP code"));

        otp.setUsed(true);
        otpCodeRepository.save(otp);
    }
    /**
     * private helpers 
     */ 

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
