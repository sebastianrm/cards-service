package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.ConsumptionRegisteredEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.domain.port.in.RegisterConsumptionUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para registrar un consumo en una tarjeta.
 * <p>
 * Para tarjetas de crédito, valida el límite disponible y autoriza el consumo.
 * Para tarjetas de débito, registra el consumo con estado pendiente.
 * Publica el evento ConsumptionRegisteredEvent tras el registro exitoso
 * e invalida la caché del límite de la tarjeta afectada.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterConsumptionServiceImpl implements RegisterConsumptionUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Consumption> execute(String cardId, BigDecimal amount, String merchantId, String description) {
        log.debug("Iniciando registro de consumo - tarjeta: {}, monto: {}, comercio: {}, descripcion: {}",
                cardId, amount, merchantId, description);

        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Monto invalido para consumo en tarjeta {}: {}", cardId, amount);
                return Single.error(new BusinessException(ErrorCode.INVALID_AMOUNT));
            }

            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para registrar consumo", cardId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMapCompletable(card -> {
                        log.debug("Tarjeta {} encontrada - estado: {}, tipo: {}",
                                card.getId(), card.getStatus(), card.getType());
                        return validateCardForConsumption(card, cardId);
                    })
                    .andThen(cardPersistencePort.findById(cardId))
                    .switchIfEmpty(Single.error(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND)))
                    .flatMap(card -> {
                        if (card.getType() == CardType.CREDIT) {
                            log.debug("Procesando consumo de credito para tarjeta {}", cardId);
                            return processCreditConsumption(card, amount, merchantId, description);
                        } else {
                            log.debug("Procesando consumo de debito para tarjeta {}", cardId);
                            return processDebitConsumption(card, amount, merchantId, description);
                        }
                    })
                    .doOnError(error ->
                            log.error("Error al registrar consumo en tarjeta {}: {}",
                                    cardId, error.getMessage()));
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al registrar consumo en tarjeta {}: [{}] {}",
                    cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al registrar consumo en tarjeta {}: {}",
                    cardId, e.getMessage(), e);
            throw new RuntimeException("Error al registrar consumo: " + e.getMessage(), e);
        }
    }

    private Completable validateCardForConsumption(Card card, String cardId) {
        if (card.getStatus() == CardStatus.CLOSED) {
            log.warn("Tarjeta {} cerrada - no se puede registrar consumo", cardId);
            return Completable.error(new BusinessException(ErrorCode.CARD_ALREADY_CLOSED));
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            log.warn("Tarjeta {} bloqueada - no se puede registrar consumo", cardId);
            return Completable.error(new BusinessException(ErrorCode.CARD_BLOCKED));
        }
        if (card.getExpiryDate() != null && card.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Tarjeta {} vencida (vencimiento: {}) - no se puede registrar consumo",
                    cardId, card.getExpiryDate());
            return Completable.error(new BusinessException(ErrorCode.CARD_EXPIRED));
        }
        log.debug("Tarjeta {} validada para consumo - estado: {}", cardId, card.getStatus());
        return Completable.complete();
    }

    private Single<Consumption> processCreditConsumption(Card card, BigDecimal amount,
            String merchantId, String description) {
        return consumptionPersistencePort.sumAmountByCardIdAndStatus(card.getId(), "AUTHORIZED")
                .flatMap(consumed -> {
                    BigDecimal availableLimit = card.getCreditLimit().subtract(consumed);
                    log.debug("Tarjeta {} - limite total: {}, consumido: {}, disponible: {}, monto solicitado: {}",
                            card.getId(), card.getCreditLimit(), consumed, availableLimit, amount);
                    if (amount.compareTo(availableLimit) > 0) {
                        log.warn("Limite de credito insuficiente para tarjeta {}: disponible={}, solicitado={}",
                                card.getId(), availableLimit, amount);
                        throw new BusinessException(ErrorCode.INSUFFICIENT_CREDIT_LIMIT);
                    }
                    return createConsumption(card, amount, merchantId, description, ConsumptionStatus.AUTHORIZED);
                });
    }

    private Single<Consumption> processDebitConsumption(Card card, BigDecimal amount,
            String merchantId, String description) {
        log.debug("Procesando consumo de debito - tarjeta: {}, monto: {}", card.getId(), amount);
        return createConsumption(card, amount, merchantId, description, ConsumptionStatus.AUTHORIZED);
    }

    private Single<Consumption> createConsumption(Card card, BigDecimal amount,
            String merchantId, String description, ConsumptionStatus status) {
        Consumption consumption = Consumption.builder()
                .id(UUID.randomUUID().toString())
                .cardId(card.getId())
                .customerId(card.getCustomerId())
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .merchantId(merchantId)
                .description(description)
                .status(status)
                .paymentId(UUID.randomUUID().toString())
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return consumptionPersistencePort.save(consumption)
                .doOnSuccess(c -> {
                    log.info("Consumo registrado exitosamente: {} en tarjeta {}, monto: {}, comercio: {}",
                            c.getId(), c.getCardId(), c.getAmount(), c.getMerchantId());
                    cardCachePort.invalidateCardLimit(c.getCardId());
                    publishConsumptionRegistered(c);
                })
                .doOnError(error ->
                        log.error("Error al persistir consumo {} en tarjeta {}: {}",
                                consumption.getId(), card.getId(), error.getMessage()));
    }

    private void publishConsumptionRegistered(Consumption consumption) {
        ConsumptionRegisteredEvent event = ConsumptionRegisteredEvent.builder()
                .consumptionId(consumption.getId())
                .cardId(consumption.getCardId())
                .customerId(consumption.getCustomerId())
                .amount(consumption.getAmount())
                .merchantId(consumption.getMerchantId())
                .description(consumption.getDescription())
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishConsumptionRegistered(event).subscribe(
                () -> log.debug("Evento ConsumptionRegistered publicado para consumo {}", consumption.getId()),
                error -> log.error("Error publicando ConsumptionRegistered: {}", error.getMessage())
        );
    }
}
