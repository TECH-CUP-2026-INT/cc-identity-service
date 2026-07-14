package co.edu.escuelaing.techcup.identity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * SCRUM-13: Configuracion de auditing de MongoDB.
 * Activa @CreatedDate y @LastModifiedDate en las entidades.
 * Separado de la clase principal para evitar conflictos con @WebMvcTest.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
