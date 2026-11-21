package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para bloquear una tarjeta.
 */
public interface BlockCardUseCase {

    /**
     * Bloquea una tarjeta activa.
     *
     * @param cardId identificador de la tarjeta
     * @param reason motivo del bloqueo
     * @return tarjeta actualizada con estado bloqueado
     */
    Single<Card> execute(String cardId, String reason);
}
