package com.yanki.cards.infrastructure.messaging.adapter;

import com.yanki.cards.domain.event.CardBlockedEvent;
import com.yanki.cards.domain.event.CardClosedEvent;
import com.yanki.cards.domain.event.CardOpenedEvent;
import com.yanki.cards.domain.event.CardUnblockedEvent;
import com.yanki.cards.domain.event.ConsumptionRegisteredEvent;
import com.yanki.cards.domain.event.ConsumptionRefundedEvent;
import com.yanki.cards.domain.event.PaymentRegisteredEvent;
import com.yanki.cards.domain.event.Topics;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import io.reactivex.rxjava3.core.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que publica eventos de dominio de tarjetas hacia Apache Kafka.
 *
 * <p>Implementa {@link CardEventPublisherPort} usando {@link KafkaTemplate} para enviar
 * eventos a los topics correspondientes. Cada metodo captura errores y los registra
 * sin propagarlos, garantizando que un fallo de Kafka no rompa el flujo de negocio.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardKafkaEventAdapter implements CardEventPublisherPort {

    private final KafkaTemplate<String, CardOpenedEvent> cardOpenedTemplate;
    private final KafkaTemplate<String, CardClosedEvent> cardClosedTemplate;
    private final KafkaTemplate<String, CardBlockedEvent> cardBlockedTemplate;
    private final KafkaTemplate<String, CardUnblockedEvent> cardUnblockedTemplate;
    private final KafkaTemplate<String, ConsumptionRegisteredEvent> consumptionRegisteredTemplate;
    private final KafkaTemplate<String, ConsumptionRefundedEvent> consumptionRefundedTemplate;
    private final KafkaTemplate<String, PaymentRegisteredEvent> paymentRegisteredTemplate;

    @Override
    public Completable publishCardOpened(CardOpenedEvent event) {
        log.info("[Kafka] Publicando evento {}: cardId={}", Topics.CARD_OPENED, event.getCardId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] CardOpenedEvent campos -> cardId={}, customerId={}, cardType={}, cardSubtype={}, creditLimit={}, linkedAccountId={}, occurredAt={}",
                event.getCardId(), event.getCustomerId(), event.getCardType(), event.getCardSubtype(), event.getCreditLimit(), event.getLinkedAccountId(), event.getOccurredAt());
        return Completable.fromFuture(
                cardOpenedTemplate.send(Topics.CARD_OPENED, event.getCardId(), event)
        ).doOnComplete(() -> log.info("Evento {} publicado para tarjeta {}", Topics.CARD_OPENED, event.getCardId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.CARD_OPENED, error.getMessage());
             return Completable.complete();
         });
    }

    @Override
    public Completable publishCardClosed(CardClosedEvent event) {
        log.info("[Kafka] Publicando evento {}: cardId={}", Topics.CARD_CLOSED, event.getCardId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] CardClosedEvent campos -> cardId={}, customerId={}, cardType={}, occurredAt={}",
                event.getCardId(), event.getCustomerId(), event.getCardType(), event.getOccurredAt());
        return Completable.fromFuture(
                cardClosedTemplate.send(Topics.CARD_CLOSED, event.getCardId(), event)
        ).doOnComplete(() -> log.info("Evento {} publicado para tarjeta {}", Topics.CARD_CLOSED, event.getCardId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.CARD_CLOSED, error.getMessage());
             return Completable.complete();
         });
    }

    @Override
    public Completable publishCardBlocked(CardBlockedEvent event) {
        log.info("[Kafka] Publicando evento {}: cardId={}", Topics.CARD_BLOCKED, event.getCardId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] CardBlockedEvent campos -> cardId={}, customerId={}, reason={}, occurredAt={}",
                event.getCardId(), event.getCustomerId(), event.getReason(), event.getOccurredAt());
        return Completable.fromFuture(
                cardBlockedTemplate.send(Topics.CARD_BLOCKED, event.getCardId(), event)
        ).doOnComplete(() -> log.info("Evento {} publicado para tarjeta {}", Topics.CARD_BLOCKED, event.getCardId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.CARD_BLOCKED, error.getMessage());
             return Completable.complete();
         });
    }

    @Override
    public Completable publishCardUnblocked(CardUnblockedEvent event) {
        log.info("[Kafka] Publicando evento {}: cardId={}", Topics.CARD_UNBLOCKED, event.getCardId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] CardUnblockedEvent campos -> cardId={}, customerId={}, occurredAt={}",
                event.getCardId(), event.getCustomerId(), event.getOccurredAt());
        return Completable.fromFuture(
                cardUnblockedTemplate.send(Topics.CARD_UNBLOCKED, event.getCardId(), event)
        ).doOnComplete(() -> log.info("Evento {} publicado para tarjeta {}", Topics.CARD_UNBLOCKED, event.getCardId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.CARD_UNBLOCKED, error.getMessage());
             return Completable.complete();
         });
    }

    @Override
    public Completable publishConsumptionRegistered(ConsumptionRegisteredEvent event) {
        log.info("[Kafka] Publicando evento {}: consumptionId={}", Topics.CONSUMPTION_REGISTERED, event.getConsumptionId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] ConsumptionRegisteredEvent campos -> consumptionId={}, cardId={}, customerId={}, amount={}, merchantId={}, description={}, occurredAt={}",
                event.getConsumptionId(), event.getCardId(), event.getCustomerId(), event.getAmount(), event.getMerchantId(), event.getDescription(), event.getOccurredAt());
        return Completable.fromFuture(
                consumptionRegisteredTemplate.send(Topics.CONSUMPTION_REGISTERED, event.getConsumptionId(), event)
        ).doOnComplete(() -> log.debug("Evento {} publicado para consumo {}", Topics.CONSUMPTION_REGISTERED, event.getConsumptionId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.CONSUMPTION_REGISTERED, error.getMessage());
             return Completable.complete();
         });
    }

    @Override
    public Completable publishConsumptionRefunded(ConsumptionRefundedEvent event) {
        log.info("[Kafka] Publicando evento {}: consumptionId={}", Topics.CONSUMPTION_REFUNDED, event.getConsumptionId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] ConsumptionRefundedEvent campos -> consumptionId={}, cardId={}, customerId={}, amount={}, occurredAt={}",
                event.getConsumptionId(), event.getCardId(), event.getCustomerId(), event.getAmount(), event.getOccurredAt());
        return Completable.fromFuture(
                consumptionRefundedTemplate.send(Topics.CONSUMPTION_REFUNDED, event.getConsumptionId(), event)
        ).doOnComplete(() -> log.debug("Evento {} publicado para consumo {}", Topics.CONSUMPTION_REFUNDED, event.getConsumptionId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.CONSUMPTION_REFUNDED, error.getMessage());
             return Completable.complete();
         });
    }

    @Override
    public Completable publishPaymentRegistered(PaymentRegisteredEvent event) {
        log.info("[Kafka] Publicando evento {}: cardId={}", Topics.PAYMENT_REGISTERED, event.getCardId());
        log.debug("[Kafka] Clase del evento publicado: {}", event.getClass().getName());
        log.debug("[Kafka] PaymentRegisteredEvent campos -> cardId={}, customerId={}, amount={}, availableLimit={}, occurredAt={}",
                event.getCardId(), event.getCustomerId(), event.getAmount(), event.getAvailableLimit(), event.getOccurredAt());
        return Completable.fromFuture(
                paymentRegisteredTemplate.send(Topics.PAYMENT_REGISTERED, event.getCardId(), event)
        ).doOnComplete(() -> log.info("Evento {} publicado para tarjeta {}", Topics.PAYMENT_REGISTERED, event.getCardId()))
         .onErrorResumeNext(error -> {
             log.error("Error publicando {}: {}", Topics.PAYMENT_REGISTERED, error.getMessage());
             return Completable.complete();
         });
    }
}
