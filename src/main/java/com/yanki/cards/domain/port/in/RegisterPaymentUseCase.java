package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

/**
 * Puerto de entrada para registrar un pago en una tarjeta de crédito.
 */
public interface RegisterPaymentUseCase {

    /**
     * Registra un pago en una tarjeta de crédito, incrementando el límite disponible.
     *
     * @param cardId identificador de la tarjeta
     * @param amount monto del pago
     * @return tarjeta actualizada
     */
    Single<Card> execute(String cardId, BigDecimal amount);
}
