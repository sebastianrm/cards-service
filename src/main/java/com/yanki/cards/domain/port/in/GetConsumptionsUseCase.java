package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Consumption;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para consultar los consumos de una tarjeta.
 */
public interface GetConsumptionsUseCase {

    /**
     * Devuelve los consumos de una tarjeta.
     *
     * @param cardId identificador de la tarjeta
     * @return flujo de consumos
     */
    Flowable<Consumption> execute(String cardId);
}
