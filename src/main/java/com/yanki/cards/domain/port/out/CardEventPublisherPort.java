package com.yanki.cards.domain.port.out;

import com.yanki.cards.domain.event.CardBlockedEvent;
import com.yanki.cards.domain.event.CardClosedEvent;
import com.yanki.cards.domain.event.CardOpenedEvent;
import com.yanki.cards.domain.event.CardUnblockedEvent;
import com.yanki.cards.domain.event.ConsumptionRegisteredEvent;
import com.yanki.cards.domain.event.ConsumptionRefundedEvent;
import com.yanki.cards.domain.event.PaymentRegisteredEvent;
import io.reactivex.rxjava3.core.Completable;

/**
 * Puerto de salida para la publicacion de eventos de dominio de tarjetas.
 *
 * <p>Define el contrato que el dominio necesita para publicar eventos
 * sin conocer la tecnologia de mensajeria subyacente (Kafka).</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public interface CardEventPublisherPort {

    /**
     * Publica evento de emision de tarjeta.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishCardOpened(CardOpenedEvent event);

    /**
     * Publica evento de cierre de tarjeta.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishCardClosed(CardClosedEvent event);

    /**
     * Publica evento de bloqueo de tarjeta.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishCardBlocked(CardBlockedEvent event);

    /**
     * Publica evento de desbloqueo de tarjeta.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishCardUnblocked(CardUnblockedEvent event);

    /**
     * Publica evento de registro de consumo.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishConsumptionRegistered(ConsumptionRegisteredEvent event);

    /**
     * Publica evento de reembolso de consumo.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishConsumptionRefunded(ConsumptionRefundedEvent event);

    /**
     * Publica evento de registro de pago.
     *
     * @param event payload del evento
     * @return completable cuando la publicacion finaliza
     */
    Completable publishPaymentRegistered(PaymentRegisteredEvent event);
}
