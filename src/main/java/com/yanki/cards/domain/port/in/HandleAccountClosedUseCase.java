package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.event.AccountClosedEvent;
import io.reactivex.rxjava3.core.Completable;

/**
 * Puerto de entrada para manejar el cierre de cuenta y cerrar
 * las tarjetas de debito vinculadas a dicha cuenta.
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public interface HandleAccountClosedUseCase {

    /**
     * Cierra todas las tarjetas de debito vinculadas a la cuenta cerrada.
     *
     * @param event evento de cierre de cuenta
     * @return completable cuando el procesamiento finaliza
     */
    Completable execute(AccountClosedEvent event);
}
