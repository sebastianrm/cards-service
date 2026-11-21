package com.yanki.cards.infrastructure.persistence.repository;

import com.yanki.cards.infrastructure.persistence.document.ProcessedEventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio Spring Data MongoDB Reactivo para documentos de eventos procesados.
 */
public interface SpringDataProcessedEventRepository
        extends ReactiveMongoRepository<ProcessedEventDocument, String> {

    Mono<Boolean> existsByTopicAndEntityIdAndOccurredAt(
            String topic, String entityId, java.time.LocalDateTime occurredAt);

    Mono<Boolean> existsByTopicAndEntityId(String topic, String entityId);

    Mono<ProcessedEventDocument> findByTopicAndEntityId(String topic, String entityId);
}
