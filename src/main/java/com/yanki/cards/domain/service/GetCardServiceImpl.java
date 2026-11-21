package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.port.in.GetCardUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener el detalle de una tarjeta por su identificador.
 * <p>
 * Utiliza el patrón cache-aside: primero consulta la caché Redis;
 * si no encuentra el valor, recurre a MongoDB y almacena el resultado en caché.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetCardServiceImpl implements GetCardUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Card> execute(String cardId) {
        log.debug("Consultando tarjeta: {}", cardId);

        try {
            return cardCachePort.getCard(cardId)
                    .doOnSuccess(card ->
                            log.debug("Tarjeta {} encontrada en caché Redis", cardId))
                    .switchIfEmpty(
                            cardPersistencePort.findById(cardId)
                                    .doOnSuccess(card ->
                                            log.debug("Tarjeta {} encontrada en MongoDB", cardId))
                                    .doOnError(error ->
                                            log.debug("Tarjeta {} no encontrada en MongoDB", cardId))
                    )
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada en caché ni en MongoDB", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .doOnSuccess(card -> {
                        log.info("Tarjeta consultada exitosamente: {}, tipo: {}, estado: {}, cliente: {}",
                                card.getId(), card.getType(), card.getStatus(), card.getCustomerId());
                        cardCachePort.setCard(card);
                    })
                    .doOnError(error ->
                            log.error("Error al consultar tarjeta {}: {}", cardId, error.getMessage()));
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al consultar tarjeta {}: [{}] {}",
                    cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al consultar tarjeta {}: {}", cardId, e.getMessage(), e);
            throw new RuntimeException("Error al consultar tarjeta: " + e.getMessage(), e);
        }
    }
}
