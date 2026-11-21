package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Consumption;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para procesar el reverso (refund) de un consumo.
 */
public interface RefundConsumptionUseCase {

    /**
     * Procesa el reverso de un consumo.
     *
     * @param cardId identificador de la tarjeta
     * @param consumptionId identificador del consumo
     * @return consumo actualizado con estado reembolsado
     */
    Single<Consumption> execute(String cardId, String consumptionId);
}
