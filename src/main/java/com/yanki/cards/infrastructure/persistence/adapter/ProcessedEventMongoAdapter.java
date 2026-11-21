package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.ProcessedEvent;
import com.yanki.cards.domain.port.out.ProcessedEventPersistencePort;
import com.yanki.cards.infrastructure.persistence.mapper.ProcessedEventDocumentMapper;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataProcessedEventRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Adaptador de salida que implementa la persistencia de eventos procesados sobre MongoDB Reactivo.
 * <p>
 * Convierte entre los tipos reactivos de Project Reactor (Mono) usados por Spring Data
 * y los tipos de RxJava 3 (Single/Maybe) requeridos por el puerto de dominio.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedEventMongoAdapter implements ProcessedEventPersistencePort {

    private final SpringDataProcessedEventRepository repository;

    @Override
    public Single<ProcessedEvent> save(ProcessedEvent event) {
        log.debug("Guardando evento procesado en MongoDB - topic: {}, entity: {}",
                event.getTopic(), event.getEntityId());
        return RxJava3Adapter.monoToSingle(
                        repository.save(ProcessedEventDocumentMapper.toDocument(event))
                                .map(ProcessedEventDocumentMapper::toDomain))
                .doOnSuccess(saved ->
                        log.debug("Evento procesado guardado - topic: {}, entity: {}",
                                event.getTopic(), event.getEntityId()))
                .doOnError(e ->
                        log.error("Error al guardar evento procesado - topic: {}, entity: {}: {}",
                                event.getTopic(), event.getEntityId(), e.getMessage(), e));
    }

    @Override
    public Single<Boolean> existsByTopicAndEntityId(String topic, String entityId) {
        log.debug("Verificando existencia de evento procesado - topic: {}, entity: {}", topic, entityId);
        return RxJava3Adapter.monoToSingle(repository.existsByTopicAndEntityId(topic, entityId))
                .doOnSuccess(exists ->
                        log.debug("Evento procesado existe: {} (topic: {}, entity: {})", exists, topic, entityId))
                .doOnError(e ->
                        log.error("Error al verificar existencia de evento - topic: {}, entity: {}: {}",
                                topic, entityId, e.getMessage(), e));
    }

    @Override
    public Maybe<ProcessedEvent> findByTopicAndEntityId(String topic, String entityId) {
        log.debug("Buscando evento procesado - topic: {}, entity: {}", topic, entityId);
        return RxJava3Adapter.monoToMaybe(
                        repository.findByTopicAndEntityId(topic, entityId)
                                .map(ProcessedEventDocumentMapper::toDomain))
                .doOnSuccess(event ->
                        log.debug("Evento procesado {}: (topic: {}, entity: {})",
                                event != null ? "encontrado" : "no encontrado", topic, entityId))
                .doOnError(e ->
                        log.error("Error al buscar evento procesado - topic: {}, entity: {}: {}",
                                topic, entityId, e.getMessage(), e));
    }
}
