package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.port.in.GetConsumptionsUseCase;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para consultar los consumos de una tarjeta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetConsumptionsServiceImpl implements GetConsumptionsUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;

    @Override
    public Flowable<Consumption> execute(String cardId) {
        log.debug("Consultando consumos de tarjeta: {}", cardId);

        try {
            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para consultar consumos", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .doOnSuccess(card ->
                            log.debug("Tarjeta {} encontrada - obteniendo consumos", cardId))
                    .toFlowable()
                    .flatMap(card -> consumptionPersistencePort.findByCardId(cardId))
                    .doOnComplete(() ->
                            log.info("Consumos consultados exitosamente para tarjeta: {}", cardId))
                    .doOnError(error ->
                            log.error("Error al consultar consumos de tarjeta {}: {}",
                                    cardId, error.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al consultar consumos de tarjeta {}: {}",
                    cardId, e.getMessage(), e);
            throw new RuntimeException("Error al consultar consumos: " + e.getMessage(), e);
        }
    }
}
