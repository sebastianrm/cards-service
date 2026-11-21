package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de salida para la caché de tarjetas.
 *
 * <p>Define el contrato que el dominio necesita para acceder
 * a la caché de tarjetas sin conocer la tecnologia subyacente (Redis).</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public interface CardCachePort {

    /**
     * Obtiene la tarjeta en caché por su identificador.
     *
     * @param cardId identificador de la tarjeta
     * @return tarjeta en caché o vacío
     */
    Maybe<Card> getCard(String cardId);

    /**
     * Almacena la tarjeta en caché.
     *
     * @param card entidad de tarjeta a almacenar
     */
    void setCard(Card card);

    /**
     * Invalida la tarjeta en caché.
     *
     * @param cardId identificador de la tarjeta
     */
    void invalidateCard(String cardId);

    /**
     * Obtiene el límite disponible en caché para una tarjeta.
     *
     * @param cardId identificador de la tarjeta
     * @return limite disponible en caché o vacío
     */
    Maybe<String> getCardLimit(String cardId);

    /**
     * Almacena el límite disponible en caché.
     *
     * @param cardId identificador de la tarjeta
     * @param limitJson limite serializado en JSON
     */
    void setCardLimit(String cardId, String limitJson);

    /**
     * Invalida el límite en caché.
     *
     * @param cardId identificador de la tarjeta
     */
    void invalidateCardLimit(String cardId);
}
