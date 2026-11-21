package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.model.ProcessedEvent;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de salida para la persistencia de eventos procesados.
 * <p>
 * Permite registrar y consultar eventos ya procesados para garantizar
 * idempotencia en el consumo de eventos Kafka.
 * </p>
 */
public interface ProcessedEventPersistencePort {

    /**
     * Guarda un evento procesado.
     *
     * @param event evento a persistir
     * @return evento persistido
     */
    Single<ProcessedEvent> save(ProcessedEvent event);

    /**
     * Verifica si un evento ya fue procesado.
     *
     * @param topic topic del evento
     * @param entityId identificador de la entidad relacionada
     * @return verdadero si ya fue procesado
     */
    Single<Boolean> existsByTopicAndEntityId(String topic, String entityId);

    /**
     * Busca un evento procesado por topic y entidad.
     *
     * @param topic topic del evento
     * @param entityId identificador de la entidad relacionada
     * @return evento encontrado o vacío
     */
    Maybe<ProcessedEvent> findByTopicAndEntityId(String topic, String entityId);
}
