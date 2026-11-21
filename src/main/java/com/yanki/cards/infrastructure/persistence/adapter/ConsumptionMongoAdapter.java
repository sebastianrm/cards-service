package com.yanki.cards.infrastructure.persistence.adapter;

import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import com.yanki.cards.infrastructure.persistence.mapper.ConsumptionDocumentMapper;
import com.yanki.cards.infrastructure.persistence.repository.SpringDataConsumptionRepository;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava3Adapter;

/**
 * Adaptador de salida que implementa la persistencia de consumos sobre MongoDB Reactivo.
 * <p>
 * Convierte entre los tipos reactivos de Project Reactor (Mono/Flux) usados por Spring Data
 * y los tipos de RxJava 3 (Single/Maybe/Flowable) requeridos por el puerto de dominio.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumptionMongoAdapter implements ConsumptionPersistencePort {

    private final SpringDataConsumptionRepository repository;

    @Override
    public Maybe<Consumption> findById(String id) {
        log.debug("Buscando consumo en MongoDB: {}", id);
        return RxJava3Adapter.monoToMaybe(repository.findById(id).map(ConsumptionDocumentMapper::toDomain))
                .doOnSuccess(consumption ->
                        log.debug("Consumo {}: {}", id, consumption != null ? "encontrado" : "no encontrado"))
                .doOnError(e ->
                        log.error("Error al buscar consumo {} en MongoDB: {}", id, e.getMessage(), e));
    }

    @Override
    public Maybe<Consumption> findByIdAndCardId(String consumptionId, String cardId) {
        log.debug("Buscando consumo {} en tarjeta {} en MongoDB", consumptionId, cardId);
        return RxJava3Adapter.monoToMaybe(
                        repository.findByIdAndCardId(consumptionId, cardId)
                                .map(ConsumptionDocumentMapper::toDomain))
                .doOnSuccess(consumption ->
                        log.debug("Consumo {} en tarjeta {}: {}", consumptionId, cardId,
                                consumption != null ? "encontrado" : "no encontrado"))
                .doOnError(e ->
                        log.error("Error al buscar consumo {} en tarjeta {} en MongoDB: {}",
                                consumptionId, cardId, e.getMessage(), e));
    }

    @Override
    public Single<Consumption> save(Consumption consumption) {
        log.debug("Guardando consumo en MongoDB: {}", consumption.getId());
        return RxJava3Adapter.monoToSingle(
                        repository.save(ConsumptionDocumentMapper.toDocument(consumption))
                                .map(ConsumptionDocumentMapper::toDomain))
                .doOnSuccess(saved ->
                        log.debug("Consumo {} guardado exitosamente en MongoDB", consumption.getId()))
                .doOnError(e ->
                        log.error("Error al guardar consumo {} en MongoDB: {}", consumption.getId(), e.getMessage(), e));
    }

    @Override
    public Flowable<Consumption> findByCardId(String cardId) {
        log.debug("Buscando consumos por tarjeta en MongoDB: {}", cardId);
        return Flowable.fromPublisher(
                repository.findByCardId(cardId).map(ConsumptionDocumentMapper::toDomain))
                .doOnComplete(() -> log.debug("Consulta de consumos por tarjeta {} completada", cardId))
                .doOnError(error ->
                        log.error("Error al buscar consumos por tarjeta {} en MongoDB: {}",
                                cardId, error.getMessage()));
    }

    @Override
    public Flowable<Consumption> findByCardIdAndStatus(String cardId, String status) {
        log.debug("Buscando consumos por tarjeta {} y estado {} en MongoDB", cardId, status);
        return Flowable.fromPublisher(
                repository.findByCardIdAndStatus(cardId, status).map(ConsumptionDocumentMapper::toDomain))
                .doOnComplete(() -> log.debug("Consulta de consumos por tarjeta {} y estado {} completada",
                        cardId, status))
                .doOnError(error ->
                        log.error("Error al buscar consumos por tarjeta {} y estado {} en MongoDB: {}",
                                cardId, status, error.getMessage()));
    }

    @Override
    public Single<BigDecimal> sumAmountByCardIdAndStatus(String cardId, String status) {
        log.debug("Calculando suma de consumos - tarjeta: {}, estado: {}", cardId, status);
        return RxJava3Adapter.monoToSingle(
                        repository.sumAmountByCardIdAndStatus(cardId, status)
                                .defaultIfEmpty(BigDecimal.ZERO))
                .doOnSuccess(sum ->
                        log.debug("Suma de consumos - tarjeta: {}, estado: {}, total: {}", cardId, status, sum))
                .doOnError(e ->
                        log.error("Error al calcular suma de consumos - tarjeta {}: {}", cardId, e.getMessage(), e));
    }
}
