package com.yanki.cards.infrastructure.persistence.repository;

import com.yanki.cards.infrastructure.persistence.document.CustomerProfileViewDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio Spring Data MongoDB Reactivo para documentos de vista de perfil de cliente.
 */
public interface SpringDataCustomerProfileViewRepository
        extends ReactiveMongoRepository<CustomerProfileViewDocument, String> {

    Mono<CustomerProfileViewDocument> findByCustomerId(String customerId);
}
