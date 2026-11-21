package com.yanki.cards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para el servicio de tarjetas.
 * <p>
 * Define la información general del API, los servidores disponibles
 * y la configuración global de SpringDoc para la documentación interactiva.
 * </p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI cardsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Yanki Cards Service API")
                        .version("1.0.0")
                        .description(
                                "API REST reactiva para la gestión de tarjetas de crédito y débito de la plataforma bancaria Yanki.\n"
                                        + "\n"
                                        + "**Funcionalidades principales:**\n"
                                        + "- Emisión de tarjetas de crédito y débito\n"
                                        + "- Registro y consulta de consumos\n"
                                        + "- Procesamiento de pagos y reembolsos\n"
                                        + "- Bloqueo y desbloqueo de tarjetas\n"
                                        + "- Consulta de límites y estados de cuenta\n"
                                        + "- Cierre de tarjetas\n"
                                        + "\n"
                                        + "**Arquitectura:** Hexagonal con puertos y adaptadores.\n"
                                        + "**Stack:** Spring WebFlux, MongoDB Reactivo, RxJava 3, Kafka, Redis."
                        )
                        .contact(new Contact()
                                .name("Equipo Yanki Cards")
                                .email("cards-team@yanki.com"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://yanki.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Entorno local de desarrollo"),
                        new Server()
                                .url("https://cards-staging.yanki.com")
                                .description("Entorno de staging"),
                        new Server()
                                .url("https://cards.yanki.com")
                                .description("Entorno de producción")
                ));
    }
}
