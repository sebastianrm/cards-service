package com.yanki.cards.infrastructure.persistence.repository;

import com.yanki.cards.infrastructure.persistence.document.CustomerRiskViewDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio Spring Data MongoDB Reactivo para documentos de vista de riesgo de cliente.
 */
public interface SpringDataCustomerRiskViewRepository
        extends ReactiveMongoRepository<CustomerRiskViewDocument, String> {

    Mono<CustomerRiskViewDocument> findByCustomerId(String customerId);
}
