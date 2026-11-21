package com.yanki.cards.domain.port.in;

import io.reactivex.rxjava3.core.Completable;

/**
 * Puerto de entrada para cerrar una tarjeta.
 */
public interface CloseCardUseCase {

    /**
     * Cierra una tarjeta de crédito o débito.
     * <p>
     * Para tarjetas de crédito, valida que no haya saldo pendiente.
     * </p>
     *
     * @param cardId identificador de la tarjeta
     * @return completado cuando la tarjeta se cierra exitosamente
     */
    Completable execute(String cardId);
}
