package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.AccountClosedEvent;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.in.HandleAccountClosedUseCase;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.event.CardClosedEvent;
import io.reactivex.rxjava3.core.Completable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso encargado de manejar el cierre de cuenta y cerrar
 * las tarjetas de debito vinculadas a dicha cuenta.
 *
 * <p>Cuando accounts-service publica un evento de cierre de cuenta,
 * este caso de uso busca todas las tarjetas de debito vinculadas
 * a la cuenta cerrada y las marca como CLOSED.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HandleAccountClosedServiceImpl implements HandleAccountClosedUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Completable execute(AccountClosedEvent event) {
        log.info("Procesando cierre de cuenta {} - buscando tarjetas de debito vinculadas", event.getAccountId());

        try {
            return cardPersistencePort.findByLinkedAccountId(event.getAccountId())
                    .filter(card -> {
                        boolean matches = card.getType() == CardType.DEBIT && card.getStatus() != CardStatus.CLOSED;
                        if (matches) {
                            log.debug("Tarjeta de debito activa encontrada vinculada a cuenta {}: {}",
                                    event.getAccountId(), card.getId());
                        }
                        return matches;
                    })
                    .toList()
                    .flatMapCompletable(cards -> {
                        if (cards.isEmpty()) {
                            log.info("No se encontraron tarjetas de debito activas para cuenta {}",
                                    event.getAccountId());
                            return Completable.complete();
                        }
                        log.info("Se encontraron {} tarjetas de debito activas para cuenta {} - procediendo con cierre",
                                cards.size(), event.getAccountId());
                        return io.reactivex.rxjava3.core.Flowable.fromIterable(cards)
                                .concatMapCompletable(card -> closeDebitCard(card, event));
                    })
                    .doOnComplete(() ->
                            log.info("Cierre de cuenta {} procesado exitosamente", event.getAccountId()))
                    .doOnError(error ->
                            log.error("Error al procesar cierre de cuenta {}: {}",
                                    event.getAccountId(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al procesar cierre de cuenta {}: {}",
                    event.getAccountId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar cierre de cuenta: " + e.getMessage(), e);
        }
    }

    private Completable closeDebitCard(Card card, AccountClosedEvent event) {
        log.debug("Cerrando tarjeta de debito {} por cierre de cuenta {}", card.getId(), event.getAccountId());
        Card closedCard = Card.builder()
                .id(card.getId())
                .customerId(card.getCustomerId())
                .type(card.getType())
                .subtype(card.getSubtype())
                .status(CardStatus.CLOSED)
                .cardNumber(card.getCardNumber())
                .expiryDate(card.getExpiryDate())
                .cvvHash(card.getCvvHash())
                .creditLimit(card.getCreditLimit())
                .availableLimit(card.getAvailableLimit())
                .linkedAccountId(card.getLinkedAccountId())
                .blockReason("Cuenta vinculada cerrada: " + event.getReason())
                .issuedAt(card.getIssuedAt())
                .closedAt(LocalDateTime.now())
                .createdAt(card.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        return cardPersistencePort.save(closedCard)
                .ignoreElement()
                .andThen(publishClosedEvent(card, event))
                .doOnComplete(() -> {
                    cardCachePort.invalidateCard(card.getId());
                    cardCachePort.invalidateCardLimit(card.getId());
                    log.info("Tarjeta de debito {} cerrada exitosamente por cierre de cuenta {}",
                            card.getId(), event.getAccountId());
                })
                .doOnError(error ->
                        log.error("Error al cerrar tarjeta de debito {} por cierre de cuenta {}: {}",
                                card.getId(), event.getAccountId(), error.getMessage()));
    }

    private Completable publishClosedEvent(Card card, AccountClosedEvent event) {
        CardClosedEvent closedEvent = CardClosedEvent.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .cardType(card.getType().name())
                .reason("Cuenta vinculada cerrada: " + event.getReason())
                .occurredAt(LocalDateTime.now())
                .build();
        return eventPublisherPort.publishCardClosed(closedEvent);
    }
}
