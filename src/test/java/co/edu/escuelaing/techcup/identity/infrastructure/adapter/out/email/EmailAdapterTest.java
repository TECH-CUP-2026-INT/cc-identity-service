package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EmailAdapter(mailSender);
        ReflectionTestUtils.setField(adapter, "fromEmail", "noreply@techcup.com");
    }

    @Test
    void sendOtpBuildsOtpEmailMessage() {
        adapter.sendOtp("student@escuelaing.edu.co", "123456");

        SimpleMailMessage message = captureMessage();
        assertThat(message.getFrom()).isEqualTo("noreply@techcup.com");
        assertThat(message.getTo()).containsExactly("student@escuelaing.edu.co");
        assertThat(message.getSubject()).contains("OTP");
        assertThat(message.getText()).contains("123456").contains("expira");
    }

    @Test
    void sendRecoveryCodeBuildsRecoveryEmailMessage() {
        adapter.sendRecoveryCode("student@escuelaing.edu.co", "ABCD1234");

        SimpleMailMessage message = captureMessage();
        assertThat(message.getTo()).containsExactly("student@escuelaing.edu.co");
        assertThat(message.getSubject()).contains("recuperación");
        assertThat(message.getText()).contains("ABCD1234").contains("un solo uso");
    }

    @Test
    void sendTemporaryCredentialsBuildsCredentialsEmailMessage() {
        adapter.sendTemporaryCredentials("referee@escuelaing.edu.co", "TempPass123");

        SimpleMailMessage message = captureMessage();
        assertThat(message.getTo()).containsExactly("referee@escuelaing.edu.co");
        assertThat(message.getSubject()).contains("Credenciales temporales");
        assertThat(message.getText()).contains("TempPass123").contains("árbitro");
    }

    private SimpleMailMessage captureMessage() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }
}
