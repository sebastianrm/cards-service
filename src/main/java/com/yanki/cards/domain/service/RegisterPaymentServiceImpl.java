package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.PaymentRegisteredEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.port.in.RegisterPaymentUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para registrar un pago en una tarjeta de crédito.
 * <p>
 * Incrementa el límite disponible de la tarjeta con el monto del pago.
 * Publica el evento PaymentRegisteredEvent tras el pago exitoso
 * e invalida la caché de la tarjeta afectada.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterPaymentServiceImpl implements RegisterPaymentUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Card> execute(String cardId, BigDecimal amount) {
        log.debug("Iniciando registro de pago - tarjeta: {}, monto: {}", cardId, amount);

        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Monto de pago invalido para tarjeta {}: {}", cardId, amount);
                return Single.error(new BusinessException(ErrorCode.INVALID_AMOUNT));
            }

            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para registrar pago", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMap(card -> {
                        if (card.getType() != CardType.CREDIT) {
                            log.warn("Tarjeta {} no es de credito (tipo: {}) - no se puede registrar pago",
                                    cardId, card.getType());
                            throw new BusinessException(ErrorCode.INVALID_CARD_TYPE);
                        }
                        if (card.getStatus() == CardStatus.CLOSED) {
                            log.warn("Tarjeta {} cerrada - no se puede registrar pago", cardId);
                            throw new BusinessException(ErrorCode.CARD_ALREADY_CLOSED);
                        }
                        log.debug("Tarjeta {} validada para pago - limite disponible actual: {}",
                                cardId, card.getAvailableLimit());
                        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
                        BigDecimal newAvailableLimit = card.getAvailableLimit().add(normalizedAmount);
                        card.setAvailableLimit(newAvailableLimit);
                        card.setUpdatedAt(LocalDateTime.now());
                        return cardPersistencePort.save(card)
                                .doOnSuccess(c -> {
                                    log.info("Pago registrado exitosamente en tarjeta {}: monto={}, nuevo limite: {}",
                                            c.getId(), amount, c.getAvailableLimit());
                                    cardCachePort.invalidateCard(c.getId());
                                    cardCachePort.invalidateCardLimit(c.getId());
                                    publishPaymentRegistered(c, normalizedAmount);
                                })
                                .doOnError(error ->
                                        log.error("Error al persistir pago en tarjeta {}: {}",
                                                cardId, error.getMessage()));
                    });
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al registrar pago en tarjeta {}: [{}] {}",
                    cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al registrar pago en tarjeta {}: {}", cardId, e.getMessage(), e);
            throw new RuntimeException("Error al registrar pago: " + e.getMessage(), e);
        }
    }

    private void publishPaymentRegistered(Card card, BigDecimal amount) {
        PaymentRegisteredEvent event = PaymentRegisteredEvent.builder()
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .amount(amount)
                .availableLimit(card.getAvailableLimit())
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishPaymentRegistered(event).subscribe(
                () -> log.debug("Evento PaymentRegistered publicado para tarjeta {}", card.getId()),
                error -> log.error("Error publicando PaymentRegistered: {}", error.getMessage())
        );
    }
}
