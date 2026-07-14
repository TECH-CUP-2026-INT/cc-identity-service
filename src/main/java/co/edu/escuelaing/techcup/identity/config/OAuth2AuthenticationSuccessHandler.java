package co.edu.escuelaing.techcup.identity.config;

import co.edu.escuelaing.techcup.identity.service.GmailLoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Handles a successful Google OAuth2 authentication.
 * Extracts the email from the OAuth2 principal, delegates business logic
 * to GmailLoginService (Phase 1), and returns a JSON response indicating
 * that an OTP has been sent.
 *
 * The JWT is NOT issued here — it is issued only after OTP verification
 * via POST /api/auth/gmail/verify-otp.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GmailLoginService gmailLoginService;
    private final ObjectMapper objectMapper;

    public OAuth2AuthenticationSuccessHandler(GmailLoginService gmailLoginService,
                                              ObjectMapper objectMapper) {
        this.gmailLoginService = gmailLoginService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        try {
            gmailLoginService.initiateLogin(email);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("success", true,
                           "message", "OTP sent to your registered email. Please verify to complete login."));
        } catch (RuntimeException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(),
                    Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
