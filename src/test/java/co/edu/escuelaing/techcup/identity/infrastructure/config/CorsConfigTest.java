package co.edu.escuelaing.techcup.identity.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void corsConfigurationAllowsConfiguredOriginsMethodsHeadersAndCredentials() {
        CorsConfigurationSource source = new CorsConfig().corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).contains("http://localhost:3000", "http://localhost:4200");
        assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).contains("*");
        assertThat(configuration.getAllowCredentials()).isTrue();
        assertThat(configuration.getMaxAge()).isEqualTo(3600L);
    }
}
