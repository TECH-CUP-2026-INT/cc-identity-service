package co.edu.escuelaing.techcup.identity.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void customOpenApiDefinesInfoAndBearerSecurityScheme() {
        OpenAPI openAPI = new SwaggerConfig().customOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("TechCup Identity Service API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("Bearer Authentication");
        assertThat(openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication").getScheme()).isEqualTo("bearer");
        assertThat(openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication").getBearerFormat()).isEqualTo("JWT");
    }
}
