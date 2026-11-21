package com.yanki.cards.domain.port.in;

import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

/**
 * Puerto de entrada para consultar el límite disponible de una tarjeta de crédito.
 */
public interface GetCardLimitUseCase {

    /**
     * Obtiene el límite disponible de una tarjeta de crédito.
     *
     * @param cardId identificador de la tarjeta
     * @return límite disponible
     */
    Single<BigDecimal> execute(String cardId);
}
