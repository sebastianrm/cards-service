package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.CardClosedEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.port.in.CloseCardUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para cerrar una tarjeta.
 * <p>
 * Para tarjetas de crédito, valida que no haya consumos autorizados pendientes.
 * Publica el evento CardClosedEvent tras el cierre exitoso
 * e invalida la caché de la tarjeta afectada.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloseCardServiceImpl implements CloseCardUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Completable execute(String cardId) {
        log.debug("Iniciando cierre de tarjeta: {}", cardId);

        try {
            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para cierre", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMapCompletable(card -> {
                        if (card.getStatus() == CardStatus.CLOSED) {
                            log.warn("Tarjeta {} ya esta cerrada - cierre duplicado", cardId);
                            return Completable.error(new BusinessException(ErrorCode.CARD_ALREADY_CLOSED));
                        }
                        log.debug("Tarjeta {} en estado {} - tipo: {} - procediendo con cierre",
                                cardId, card.getStatus(), card.getType());
                        if (card.getType() == CardType.CREDIT) {
                            return consumptionPersistencePort.sumAmountByCardIdAndStatus(cardId, "AUTHORIZED")
                                    .flatMapCompletable(consumed -> {
                                        if (consumed.compareTo(BigDecimal.ZERO) > 0) {
                                            log.warn("Tarjeta {} tiene saldo pendiente: {} - no se puede cerrar",
                                                    cardId, consumed);
                                            return Completable.error(new BusinessException(ErrorCode.OUTSTANDING_BALANCE));
                                        }
                                        log.debug("Tarjeta {} de credito sin saldo pendiente - procediendo con cierre", cardId);
                                        return closeCard(card);
                                    });
                        } else {
                            log.debug("Tarjeta {} de debito - procediendo con cierre directo", cardId);
                            return closeCard(card);
                        }
                    });
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al cerrar tarjeta {}: [{}] {}",
                    cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al cerrar tarjeta {}: {}", cardId, e.getMessage(), e);
            throw new RuntimeException("Error al cerrar tarjeta: " + e.getMessage(), e);
        }
    }

    private Completable closeCard(Card card) {
        card.setStatus(CardStatus.CLOSED);
        card.setClosedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        return cardPersistencePort.save(card)
                .flatMapCompletable(saved -> {
                    log.info("Tarjeta cerrada exitosamente: {}, tipo: {}, cliente: {}",
                            card.getId(), card.getType(), card.getCustomerId());
                    cardCachePort.invalidateCard(card.getId());
                    cardCachePort.invalidateCardLimit(card.getId());
                    publishCardClosed(card);
                    return Completable.complete();
                });
    }

    private void publishCardClosed(Card card) {
        CardClosedEvent event = CardClosedEvent.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .cardType(card.getType().name())
                .reason(card.getBlockReason())
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishCardClosed(event).subscribe(
                () -> log.debug("Evento CardClosed publicado para tarjeta {}", card.getId()),
                error -> log.error("Error publicando CardClosed: {}", error.getMessage())
        );
    }
}
