package com.yanki.cards.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Propiedades de configuracion para la caché Redis de cards-service.
 *
 * <p>Externaliza los valores de TTL y prefijo de claves para permitir
 * configuracion diferente por entorno sin recompilar.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "yanki.cache")
public class CacheProperties {

    /** Prefijo de las claves Redis. Default: yanki:cards:. */
    private String keyPrefix = "yanki:cards:";

    /** TTL para la caché de tarjeta completa. Default: 5 minutos. */
    private Duration cardTtl = Duration.ofMinutes(5);

    /** TTL para la caché de limite. Default: 2 minutos. */
    private Duration limitTtl = Duration.ofMinutes(2);
}
