package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.model.CustomerProfileView;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de salida para la persistencia de vistas de perfil de cliente.
 * <p>
 * Permite consultar y actualizar la vista local del perfil del cliente
 * mantenida por eventos inbound de Kafka.
 * </p>
 */
public interface CustomerProfileViewPersistencePort {

    /**
     * Busca la vista de perfil de un cliente por su identificador.
     *
     * @param customerId identificador del cliente
     * @return vista de perfil encontrada o vacio
     */
    Maybe<CustomerProfileView> findByCustomerId(String customerId);

    /**
     * Guarda o actualiza la vista de perfil de un cliente.
     *
     * @param profile vista de perfil a persistir
     * @return vista de perfil persistida
     */
    Single<CustomerProfileView> save(CustomerProfileView profile);
}
