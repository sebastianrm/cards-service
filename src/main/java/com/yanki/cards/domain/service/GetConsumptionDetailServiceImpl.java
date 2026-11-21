package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.port.in.GetConsumptionDetailUseCase;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener el detalle de un consumo específico.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetConsumptionDetailServiceImpl implements GetConsumptionDetailUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;

    @Override
    public Single<Consumption> execute(String cardId, String consumptionId) {
        log.debug("Consultando detalle de consumo: {} en tarjeta: {}", consumptionId, cardId);

        try {
            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para consultar consumo {}", cardId, consumptionId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMap(card ->
                            consumptionPersistencePort.findByIdAndCardId(consumptionId, cardId)
                                    .switchIfEmpty(Single.error(() -> {
                                        log.warn("Consumo {} no encontrado en tarjeta {}",
                                                consumptionId, cardId);
                                        return new BusinessException(ErrorCode.CONSUMPTION_NOT_FOUND);
                                    }))
                    )
                    .doOnSuccess(consumption ->
                            log.info("Consumo consultado exitosamente: {} de tarjeta {}, monto: {}, estado: {}",
                                    consumptionId, cardId, consumption.getAmount(), consumption.getStatus()))
                    .doOnError(error ->
                            log.error("Error al consultar consumo {} en tarjeta {}: {}",
                                    consumptionId, cardId, error.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al consultar consumo {} en tarjeta {}: {}",
                    consumptionId, cardId, e.getMessage(), e);
            throw new RuntimeException("Error al consultar consumo: " + e.getMessage(), e);
        }
    }
}
