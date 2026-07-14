package co.edu.escuelaing.techcup.identity.service;

import co.edu.escuelaing.techcup.identity.entity.OtpCodeEntity;
import co.edu.escuelaing.techcup.identity.entity.UserEntity;
import co.edu.escuelaing.techcup.identity.repository.OtpCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Servicio responsable de la generacion, envio por email y verificacion de OTP.
 * Implementa SCRUM-13: validacion de doble factor OTP.
 * Usa MongoDB a traves de OtpCodeRepository, almacenando el userId como referencia.
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
     * Genera un OTP numerico, lo guarda en MongoDB y lo envia por email.
     * Elimina OTPs expirados del usuario antes de crear uno nuevo.
     * @param user el usuario que solicito el OTP
     */
    public void generateAndSend(UserEntity user) {
        otpCodeRepository.deleteByUserIdAndExpiresAtBefore(user.getId(), LocalDateTime.now());

        String code = generateCode();

        OtpCodeEntity otp = OtpCodeEntity.builder()
                .code(code)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build();

        otpCodeRepository.save(otp);
        sendEmail(user.getEmail(), code);
    }

    /**
     * Verifica el codigo OTP para el usuario dado.
     * Marca el OTP como usado si es valido.
     * @param code el codigo OTP ingresado por el usuario
     * @param user el usuario que intenta verificar
     * @throws RuntimeException si el OTP es invalido o esta expirado
     */
    public void verify(String code, UserEntity user) {
        OtpCodeEntity otp = otpCodeRepository
                .findByCodeAndUserIdAndUsedFalseAndExpiresAtAfter(code, user.getId(), LocalDateTime.now())
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
