package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Puerto de entrada para consultar tarjetas del sistema.
 * <p>
 * Permite listar todas las tarjetas o filtrar por cliente.
 * </p>
 */
public interface FindCardsUseCase {

    /**
     * Devuelve todas las tarjetas registradas en el sistema.
     *
     * @return flujo de tarjetas
     */
    Flowable<Card> findAll();

    /**
     * Devuelve las tarjetas asociadas a un cliente.
     *
     * @param customerId identificador del cliente
     * @return flujo de tarjetas del cliente
     */
    Flowable<Card> findByCustomerId(String customerId);
}
