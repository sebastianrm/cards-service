package com.yanki.cards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the cards-service microservice.
 * <p>
 * Part of the Yanki banking platform. The service follows the
 * database-per-service pattern: it owns and accesses only its own MongoDB
 * database and never reads nor writes the data owned by another
 * microservice. Cross-service collaboration is performed through Kafka
 * events and never through direct REST calls.
 * </p>
 *
 * @author Sebastian Retamal
 * @contact semorale@gmail.com
 * @version 1.0
 * @since 2026
 */
@SpringBootApplication
public class CardsServiceApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments forwarded to the Spring context
     * @since 2026
     */
    public static void main(String[] args) {
        SpringApplication.run(CardsServiceApplication.class, args);
    }
}
