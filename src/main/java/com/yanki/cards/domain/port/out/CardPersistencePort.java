package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de salida para la persistencia de tarjetas.
 * <p>
 * Define las operaciones de acceso a datos que el dominio necesita
 * sin depender de MongoDB ni Spring Data.
 * </p>
 */
public interface CardPersistencePort {

    /**
     * Busca una tarjeta por su identificador.
     *
     * @param id identificador de la tarjeta
     * @return tarjeta encontrada o vacio
     */
    Maybe<Card> findById(String id);

    /**
     * Guarda o actualiza una tarjeta.
     *
     * @param card entidad de dominio a persistir
     * @return tarjeta persistida
     */
    Single<Card> save(Card card);

    /**
     * Devuelve todas las tarjetas.
     *
     * @return flujo de tarjetas
     */
    Flowable<Card> findAll();

    /**
     * Devuelve las tarjetas de un cliente.
     *
     * @param customerId identificador del cliente
     * @return flujo de tarjetas
     */
    Flowable<Card> findByCustomerId(String customerId);

    /**
     * Verifica si existe una tarjeta por cliente, tipo, subtipo y estado.
     *
     * @param customerId identificador del cliente
     * @param type tipo de tarjeta
     * @param subtype subtipo de tarjeta
     * @param status estado de la tarjeta
     * @return verdadero si existe
     */
    Single<Boolean> existsByCustomerIdAndTypeAndSubtypeAndStatus(String customerId, String type, String subtype, String status);

    /**
     * Verifica si existe una tarjeta por cuenta vinculada y tipo.
     *
     * @param linkedAccountId cuenta vinculada
     * @param type tipo de tarjeta
     * @return verdadero si existe
     */
    Single<Boolean> existsByLinkedAccountIdAndType(String linkedAccountId, String type);

    /**
     * Devuelve las tarjetas de debito vinculadas a una cuenta.
     *
     * @param linkedAccountId cuenta vinculada
     * @return flujo de tarjetas de debito vinculadas
     */
    Flowable<Card> findByLinkedAccountId(String linkedAccountId);
}
