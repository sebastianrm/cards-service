package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.infrastructure.persistence.mapper.CardDocumentMapper;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataCardRepository;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Adaptador de salida que implementa la persistencia de tarjetas sobre MongoDB Reactivo.
 * <p>
 * Convierte entre los tipos reactivos de Project Reactor (Mono/Flux) usados por Spring Data
 * y los tipos de RxJava 3 (Single/Maybe/Flowable) requeridos por el puerto de dominio.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardMongoAdapter implements CardPersistencePort {

    private final SpringDataCardRepository repository;

    @Override
    public Maybe<Card> findById(String id) {
        log.debug("Buscando tarjeta en MongoDB: {}", id);
        return RxJava3Adapter.monoToMaybe(repository.findById(id).map(CardDocumentMapper::toDomain))
                .doOnSuccess(card -> {
                    if (card != null) {
                        log.debug("Tarjeta {} encontrada en MongoDB", id);
                    } else {
                        log.debug("Tarjeta {} no encontrada en MongoDB", id);
                    }
                })
                .doOnError(e ->
                        log.error("Error al buscar tarjeta {} en MongoDB: {}", id, e.getMessage(), e));
    }

    @Override
    public Single<Card> save(Card card) {
        log.debug("Guardando tarjeta en MongoDB: {}", card.getId());
        return RxJava3Adapter.monoToSingle(
                        repository.save(CardDocumentMapper.toDocument(card))
                                .map(CardDocumentMapper::toDomain))
                .doOnSuccess(saved ->
                        log.debug("Tarjeta {} guardada exitosamente en MongoDB", card.getId()))
                .doOnError(e ->
                        log.error("Error al guardar tarjeta {} en MongoDB: {}", card.getId(), e.getMessage(), e));
    }

    @Override
    public Flowable<Card> findAll() {
        log.debug("Obteniendo todas las tarjetas de MongoDB");
        return Flowable.fromPublisher(repository.findAll().map(CardDocumentMapper::toDomain))
                .doOnComplete(() -> log.debug("Consulta de todas las tarjetas completada"))
                .doOnError(error -> log.error("Error al obtener todas las tarjetas de MongoDB: {}", error.getMessage()));
    }

    @Override
    public Flowable<Card> findByCustomerId(String customerId) {
        log.debug("Buscando tarjetas por cliente en MongoDB: {}", customerId);
        return Flowable.fromPublisher(repository.findByCustomerId(customerId).map(CardDocumentMapper::toDomain))
                .doOnComplete(() -> log.debug("Consulta de tarjetas por cliente {} completada", customerId))
                .doOnError(error ->
                        log.error("Error al buscar tarjetas por cliente {} en MongoDB: {}",
                                customerId, error.getMessage()));
    }

    @Override
    public Single<Boolean> existsByCustomerIdAndTypeAndSubtypeAndStatus(
            String customerId, String type, String subtype, String status) {
        log.debug("Verificando existencia de tarjeta - cliente: {}, tipo: {}, subtipo: {}, estado: {}",
                customerId, type, subtype, status);
        return RxJava3Adapter.monoToSingle(
                        repository.existsByCustomerIdAndTypeAndSubtypeAndStatus(
                                customerId, type, subtype, status))
                .doOnSuccess(exists ->
                        log.debug("Tarjeta existe: {} (cliente: {}, tipo: {}, subtipo: {}, estado: {})",
                                exists, customerId, type, subtype, status))
                .doOnError(e ->
                        log.error("Error al verificar existencia de tarjeta - cliente {}: {}",
                                customerId, e.getMessage(), e));
    }

    @Override
    public Single<Boolean> existsByLinkedAccountIdAndType(String linkedAccountId, String type) {
        log.debug("Verificando existencia de tarjeta por cuenta - cuenta: {}, tipo: {}",
                linkedAccountId, type);
        return RxJava3Adapter.monoToSingle(
                        repository.existsByLinkedAccountIdAndType(linkedAccountId, type))
                .doOnSuccess(exists ->
                        log.debug("Tarjeta existe: {} (cuenta: {}, tipo: {})", exists, linkedAccountId, type))
                .doOnError(e ->
                        log.error("Error al verificar existencia de tarjeta por cuenta {}: {}",
                                linkedAccountId, e.getMessage(), e));
    }

    @Override
    public Flowable<Card> findByLinkedAccountId(String linkedAccountId) {
        log.debug("Buscando tarjetas por cuenta en MongoDB: {}", linkedAccountId);
        return Flowable.fromPublisher(repository.findByLinkedAccountId(linkedAccountId).map(CardDocumentMapper::toDomain))
                .doOnComplete(() -> log.debug("Consulta de tarjetas por cuenta {} completada", linkedAccountId))
                .doOnError(error ->
                        log.error("Error al buscar tarjetas por cuenta {} en MongoDB: {}",
                                linkedAccountId, error.getMessage()));
    }
}
