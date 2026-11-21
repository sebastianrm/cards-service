package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.model.CustomerRiskView;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de salida para la persistencia de vistas de riesgo de cliente.
 * <p>
 * Permite consultar y actualizar la vista local de riesgo del cliente
 * (deuda vencida) mantenida por eventos inbound de Kafka.
 * </p>
 */
public interface CustomerRiskViewPersistencePort {

    /**
     * Busca la vista de riesgo de un cliente por su identificador.
     *
     * @param customerId identificador del cliente
     * @return vista de riesgo encontrada o vacio
     */
    Maybe<CustomerRiskView> findByCustomerId(String customerId);

    /**
     * Guarda o actualiza la vista de riesgo de un cliente.
     *
     * @param risk vista de riesgo a persistir
     * @return vista de riesgo persistida
     */
    Single<CustomerRiskView> save(CustomerRiskView risk);
}
