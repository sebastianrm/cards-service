package com.yanki.cards.infrastructure.persistence.repository;

import com.yanki.cards.infrastructure.persistence.document.ConsumptionDocument;
import java.math.BigDecimal;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio Spring Data MongoDB Reactivo para documentos de consumo.
 */
public interface SpringDataConsumptionRepository extends ReactiveMongoRepository<ConsumptionDocument, String> {

    Flux<ConsumptionDocument> findByCardId(String cardId);

    Mono<ConsumptionDocument> findByIdAndCardId(String consumptionId, String cardId);

    Flux<ConsumptionDocument> findByCardIdAndStatus(String cardId, String status);

    @Aggregation(pipeline = {
            "{ '$match': { 'cardId': ?0, 'status': ?1 } }",
            "{ '$group': { '_id': null, 'total': { '$sum': '$amount' } } }"
    })
    Mono<BigDecimal> sumAmountByCardIdAndStatus(String cardId, String status);
}
