package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Consumption;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para obtener el detalle de un consumo específico.
 */
public interface GetConsumptionDetailUseCase {

    /**
     * Obtiene el detalle de un consumo por su identificador y tarjeta.
     *
     * @param cardId identificador de la tarjeta
     * @param consumptionId identificador del consumo
     * @return consumo encontrado
     */
    Single<Consumption> execute(String cardId, String consumptionId);
}
