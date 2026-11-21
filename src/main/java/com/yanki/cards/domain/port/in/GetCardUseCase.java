package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para obtener el detalle de una tarjeta por su identificador.
 */
public interface GetCardUseCase {

    /**
     * Obtiene el detalle de una tarjeta por su identificador.
     *
     * @param cardId identificador de la tarjeta
     * @return tarjeta encontrada
     */
    Single<Card> execute(String cardId);
}
