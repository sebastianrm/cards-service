package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Consumption;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

/**
 * Puerto de entrada para registrar un consumo en una tarjeta.
 * <p>
 * Para tarjetas de crédito, valida el límite y autoriza el consumo.
 * Para tarjetas de débito, inicia el flujo asíncrono de pago.
 * </p>
 */
public interface RegisterConsumptionUseCase {

    /**
     * Registra un consumo en la tarjeta indicada.
     *
     * @param cardId identificador de la tarjeta
     * @param amount monto del consumo
     * @param merchantId identificador del comercio
     * @param description descripción del consumo
     * @return consumo registrado
     */
    Single<Consumption> execute(String cardId, BigDecimal amount, String merchantId, String description);
}
