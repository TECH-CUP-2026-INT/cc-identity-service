package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.email;

import co.edu.escuelaing.techcup.identity.domain.port.out.EmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtp(String email, String otpCode) {
        log.info("Sending OTP to: {}", email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("TechCup - Código de verificación OTP");
        message.setText("Tu código de verificación es: " + otpCode +
                "\n\nEste código expira en unos minutos. No compartas este código con nadie.");
        mailSender.send(message);
    }

    @Override
    public void sendRecoveryCode(String email, String recoveryCode) {
        log.info("Sending recovery code to: {}", email);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("TechCup - Código de recuperación de contraseña");
        message.setText("Tu código de recuperación es: " + recoveryCode +
                "\n\nEste código es de un solo uso y tiene tiempo limitado." +
                "\nSi no solicitaste este cambio, ignora este correo.");
        mailSender.send(message);
    }

}
