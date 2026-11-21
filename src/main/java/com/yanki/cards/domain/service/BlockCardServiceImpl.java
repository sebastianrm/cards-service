package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.CardBlockedEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.port.in.BlockCardUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para bloquear una tarjeta.
 * <p>
 * Publica el evento CardBlockedEvent tras el bloqueo exitoso
 * e invalida la caché de la tarjeta afectada.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockCardServiceImpl implements BlockCardUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Card> execute(String cardId, String reason) {
        log.debug("Iniciando bloqueo de tarjeta: {}, motivo: {}", cardId, reason);

        try {
            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para bloqueo", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMap(card -> {
                        if (card.getStatus() == CardStatus.CLOSED) {
                            log.warn("Tarjeta {} ya esta cerrada - no se puede bloquear", cardId);
                            throw new BusinessException(ErrorCode.CARD_ALREADY_CLOSED);
                        }
                        if (card.getStatus() == CardStatus.BLOCKED) {
                            log.warn("Tarjeta {} ya esta bloqueada - bloqueo duplicado", cardId);
                            throw new BusinessException(ErrorCode.CARD_ALREADY_BLOCKED);
                        }
                        log.debug("Tarjeta {} en estado {} - procediendo con bloqueo", cardId, card.getStatus());
                        card.setStatus(CardStatus.BLOCKED);
                        card.setBlockedAt(LocalDateTime.now());
                        card.setBlockReason(reason);
                        card.setUpdatedAt(LocalDateTime.now());
                        return cardPersistencePort.save(card)
                                .doOnSuccess(c -> {
                                    log.info("Tarjeta bloqueada exitosamente: {}, motivo: {}, cliente: {}",
                                            c.getId(), reason, c.getCustomerId());
                                    cardCachePort.invalidateCard(c.getId());
                                    cardCachePort.invalidateCardLimit(c.getId());
                                    publishCardBlocked(c, reason);
                                })
                                .doOnError(error ->
                                        log.error("Error al persistir bloqueo de tarjeta {}: {}",
                                                cardId, error.getMessage()));
                    });
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al bloquear tarjeta {}: [{}] {}",
                    cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al bloquear tarjeta {}: {}", cardId, e.getMessage(), e);
            throw new RuntimeException("Error al bloquear tarjeta: " + e.getMessage(), e);
        }
    }

    private void publishCardBlocked(Card card, String reason) {
        CardBlockedEvent event = CardBlockedEvent.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .reason(reason)
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishCardBlocked(event).subscribe(
                () -> log.debug("Evento CardBlocked publicado para tarjeta {}", card.getId()),
                error -> log.error("Error publicando CardBlocked: {}", error.getMessage())
        );
    }
}
