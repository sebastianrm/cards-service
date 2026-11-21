package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.model.Consumption;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de salida para la persistencia de consumos.
 * <p>
 * Define las operaciones de acceso a datos de consumos que el dominio necesita
 * sin depender de MongoDB ni Spring Data.
 * </p>
 */
public interface ConsumptionPersistencePort {

    /**
     * Busca un consumo por su identificador.
     *
     * @param id identificador del consumo
     * @return consumo encontrado o vacío
     */
    Maybe<Consumption> findById(String id);

    /**
     * Busca un consumo por su identificador y tarjeta asociada.
     *
     * @param consumptionId identificador del consumo
     * @param cardId identificador de la tarjeta
     * @return consumo encontrado o vacío
     */
    Maybe<Consumption> findByIdAndCardId(String consumptionId, String cardId);

    /**
     * Guarda o actualiza un consumo.
     *
     * @param consumption entidad de dominio a persistir
     * @return consumo persistido
     */
    Single<Consumption> save(Consumption consumption);

    /**
     * Devuelve los consumos de una tarjeta.
     *
     * @param cardId identificador de la tarjeta
     * @return flujo de consumos
     */
    Flowable<Consumption> findByCardId(String cardId);

    /**
     * Devuelve los consumos autorizados de una tarjeta.
     *
     * @param cardId identificador de la tarjeta
     * @return flujo de consumos autorizados
     */
    Flowable<Consumption> findByCardIdAndStatus(String cardId, String status);

    /**
     * Calcula la suma de montos de consumos autorizados de una tarjeta.
     *
     * @param cardId identificador de la tarjeta
     * @return suma de montos autorizados
     */
    Single<java.math.BigDecimal> sumAmountByCardIdAndStatus(String cardId, String status);
}
