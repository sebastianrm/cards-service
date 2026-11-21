package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para desbloquear una tarjeta.
 */
public interface UnblockCardUseCase {

    /**
     * Desbloquea una tarjeta bloqueada, restaurándola a estado activo.
     *
     * @param cardId identificador de la tarjeta
     * @return tarjeta actualizada con estado activo
     */
    Single<Card> execute(String cardId);
}
