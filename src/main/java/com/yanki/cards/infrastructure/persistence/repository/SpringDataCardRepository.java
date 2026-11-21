package com.yanki.cards.infrastructure.persistence.repository;

import com.yanki.cards.infrastructure.persistence.document.CardDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio Spring Data MongoDB Reactivo para documentos de tarjeta.
 * <p>
 * Vive en la capa de infraestructura y no debe ser importado por el dominio.
 * </p>
 */
public interface SpringDataCardRepository extends ReactiveMongoRepository<CardDocument, String> {

    /**
     * Busca una tarjeta por su numero.
     */
    Mono<CardDocument> findByCardNumber(String cardNumber);

    /**
     * Busca todas las tarjetas de un cliente.
     */
    Flux<CardDocument> findByCustomerId(String customerId);

    /**
     * Verifica existencia por cliente, tipo, subtipo y estado.
     */
    Mono<Boolean> existsByCustomerIdAndTypeAndSubtypeAndStatus(
            String customerId, String type, String subtype, String status);

    /**
     * Verifica existencia por cuenta vinculada y tipo.
     */
    Mono<Boolean> existsByLinkedAccountIdAndType(String linkedAccountId, String type);

    /**
     * Busca todas las tarjetas vinculadas a una cuenta.
     */
    Flux<CardDocument> findByLinkedAccountId(String linkedAccountId);
}
