package co.edu.escuelaing.techcup.identity.infrastructure.config.security;

import co.edu.escuelaing.techcup.identity.domain.port.out.RevokedTokenRepositoryPort;
import co.edu.escuelaing.techcup.identity.domain.port.out.SessionActivityRepositoryPort;
import co.edu.escuelaing.techcup.identity.shared.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/otp/**",
            "/api/v1/password/**",
            "/api/v1/token/validate",
            "/api/v1/internal/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    // Consumido por am-notification-service para resolver el email de un recipientId
    // (ver RecipientEmailResolver de ese servicio). A diferencia del resto de
    // /api/v1/internal/**, expone un dato personal (email) a un servicio externo,
    // así que requiere la API key interna en vez de heredar el permitAll general.
    private static final String INTERNAL_USER_EMAIL_ENDPOINT = "/api/v1/internal/credentials/*/email";

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtil jwtUtil,
            RevokedTokenRepositoryPort revokedTokenRepository,
            SessionActivityRepositoryPort sessionActivityRepository,
            @Value("${auth.inactivity-timeout-minutes:30}") int inactivityTimeoutMinutes) {
        return new JwtAuthenticationFilter(jwtUtil, revokedTokenRepository, sessionActivityRepository, inactivityTimeoutMinutes);
    }

    @Bean
    public InternalApiKeyFilter internalApiKeyFilter(@Value("${techcup.security.internal.api-key:}") String internalApiKey) {
        return new InternalApiKeyFilter(internalApiKey);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalApiKeyFilter internalApiKeyFilter) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                        // Login/OTP/password (pre-authentication) and internal service-to-service
                        // calls (X-Internal-Api-Key) don't carry a browser session/cookie, so the
                        // CSRF handshake doesn't apply to them - same endpoints already permitAll below.
                        .ignoringRequestMatchers(PUBLIC_ENDPOINTS)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, INTERNAL_USER_EMAIL_ENDPOINT).hasRole("SERVICIO_INTERNO")
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
