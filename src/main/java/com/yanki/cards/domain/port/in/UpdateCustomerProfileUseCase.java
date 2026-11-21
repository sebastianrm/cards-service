package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.event.CustomerRegisteredEvent;
import io.reactivex.rxjava3.core.Completable;

/**
 * Puerto de entrada para actualizar la vista del perfil del cliente
 * a partir de eventos Kafka recibidos de customers-service.
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public interface UpdateCustomerProfileUseCase {

    /**
     * Actualiza o crea la vista local del perfil del cliente.
     *
     * @param event evento de registro de cliente
     * @return completable cuando la actualizacion finaliza
     */
    Completable execute(CustomerRegisteredEvent event);
}
