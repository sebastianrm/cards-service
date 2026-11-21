package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.in.GetCardLimitUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para consultar el límite disponible de una tarjeta de crédito.
 * <p>
 * Calcula el límite disponible restando los consumos autorizados del crédito total.
 * Utiliza el patrón cache-aside: primero consulta la caché Redis;
 * si no encuentra el valor, recurre a MongoDB y almacena el resultado en caché.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetCardLimitServiceImpl implements GetCardLimitUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<BigDecimal> execute(String cardId) {
        log.debug("Consultando limite disponible de tarjeta: {}", cardId);

        try {
            return cardCachePort.getCardLimit(cardId)
                    .map(BigDecimal::new)
                    .doOnSuccess(limit ->
                            log.debug("Limite de tarjeta {} encontrado en caché: {}", cardId, limit))
                    .switchIfEmpty(calculateAndCacheLimit(cardId));
        } catch (Exception e) {
            log.error("Error inesperado al consultar limite de tarjeta {}: {}", cardId, e.getMessage(), e);
            throw new RuntimeException("Error al consultar limite: " + e.getMessage(), e);
        }
    }

    private Single<BigDecimal> calculateAndCacheLimit(String cardId) {
        log.debug("Caché vacía para límite de tarjeta {}, calculando desde MongoDB", cardId);
        return cardPersistencePort.findById(cardId)
                .switchIfEmpty(Single.error(() -> {
                    log.warn("Tarjeta {} no encontrada para consultar limite", cardId);
                    return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                }))
                .flatMap(card -> {
                    if (card.getType() != CardType.CREDIT) {
                        log.warn("Tarjeta {} no es de credito (tipo: {}) - limite no aplicable",
                                cardId, card.getType());
                        throw new BusinessException(ErrorCode.LIMIT_NOT_APPLICABLE);
                    }
                    log.debug("Calculando limite para tarjeta {} - limite total: {}", cardId, card.getCreditLimit());
                    return consumptionPersistencePort.sumAmountByCardIdAndStatus(cardId, "AUTHORIZED")
                            .map(consumed -> {
                                BigDecimal available = card.getCreditLimit().subtract(consumed);
                                log.debug("Tarjeta {} - consumos autorizados: {}, limite disponible: {}",
                                        cardId, consumed, available);
                                return available;
                            });
                })
                .doOnSuccess(limit -> {
                    log.info("Limite disponible de tarjeta {}: {}", cardId, limit);
                    cardCachePort.setCardLimit(cardId, limit.toPlainString());
                })
                .doOnError(error ->
                        log.error("Error al calcular limite de tarjeta {}: {}", cardId, error.getMessage()));
    }
}
