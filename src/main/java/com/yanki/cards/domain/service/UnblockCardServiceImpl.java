package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.CardUnblockedEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.port.in.UnblockCardUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para desbloquear una tarjeta.
 * <p>
 * Publica el evento CardUnblockedEvent tras el desbloqueo exitoso
 * e invalida la caché de la tarjeta afectada.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnblockCardServiceImpl implements UnblockCardUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Card> execute(String cardId) {
        log.debug("Iniciando desbloqueo de tarjeta: {}", cardId);

        try {
            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para desbloqueo", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMap(card -> {
                        if (card.getStatus() == CardStatus.CLOSED) {
                            log.warn("Tarjeta {} ya esta cerrada - no se puede desbloquear", cardId);
                            throw new BusinessException(ErrorCode.CARD_ALREADY_CLOSED);
                        }
                        if (card.getStatus() != CardStatus.BLOCKED) {
                            log.warn("Tarjeta {} no esta bloqueada (estado: {}) - no se puede desbloquear",
                                    cardId, card.getStatus());
                            throw new BusinessException(ErrorCode.CARD_NOT_BLOCKED);
                        }
                        log.debug("Tarjeta {} en estado BLOCKED - procediendo con desbloqueo", cardId);
                        card.setStatus(CardStatus.ACTIVE);
                        card.setBlockedAt(null);
                        card.setBlockReason(null);
                        card.setUpdatedAt(LocalDateTime.now());
                        return cardPersistencePort.save(card)
                                .doOnSuccess(c -> {
                                    log.info("Tarjeta desbloqueada exitosamente: {}, cliente: {}",
                                            c.getId(), c.getCustomerId());
                                    cardCachePort.invalidateCard(c.getId());
                                    cardCachePort.invalidateCardLimit(c.getId());
                                    publishCardUnblocked(c);
                                })
                                .doOnError(error ->
                                        log.error("Error al persistir desbloqueo de tarjeta {}: {}",
                                                cardId, error.getMessage()));
                    });
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al desbloquear tarjeta {}: [{}] {}",
                    cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al desbloquear tarjeta {}: {}", cardId, e.getMessage(), e);
            throw new RuntimeException("Error al desbloquear tarjeta: " + e.getMessage(), e);
        }
    }

    private void publishCardUnblocked(Card card) {
        CardUnblockedEvent event = CardUnblockedEvent.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishCardUnblocked(event).subscribe(
                () -> log.debug("Evento CardUnblocked publicado para tarjeta {}", card.getId()),
                error -> log.error("Error publicando CardUnblocked: {}", error.getMessage())
        );
    }
}
