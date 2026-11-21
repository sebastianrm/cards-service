package com.yanki.cards.domain.service;

import com.yanki.cards.domain.event.ConsumptionRefundedEvent;
import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.domain.port.in.RefundConsumptionUseCase;
import com.yanki.cards.domain.port.out.CardCachePort;
import com.yanki.cards.domain.port.out.CardEventPublisherPort;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para procesar el reverso (refund) de un consumo.
 * <p>
 * Solo se pueden reembolsar consumos en estado AUTHORIZED o CONFIRMED.
 * Publica el evento ConsumptionRefundedEvent tras el reembolso exitoso
 * e invalida la caché del límite de la tarjeta afectada.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundConsumptionServiceImpl implements RefundConsumptionUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;
    private final CardEventPublisherPort eventPublisherPort;
    private final CardCachePort cardCachePort;

    @Override
    public Single<Consumption> execute(String cardId, String consumptionId) {
        log.debug("Iniciando reembolso - tarjeta: {}, consumo: {}", cardId, consumptionId);

        try {
            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para reembolso del consumo {}", cardId, consumptionId);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .flatMap(card ->
                            consumptionPersistencePort.findByIdAndCardId(consumptionId, cardId)
                                    .switchIfEmpty(Single.error(() -> {
                                        log.warn("Consumo {} no encontrado en tarjeta {}",
                                                consumptionId, cardId);
                                        return new BusinessException(ErrorCode.CONSUMPTION_NOT_FOUND);
                                    }))
                    )
                    .flatMap(consumption -> {
                        if (consumption.getStatus() == ConsumptionStatus.REFUNDED) {
                            log.warn("Consumo {} ya fue reembolsado - reembolso duplicado", consumptionId);
                            throw new BusinessException(ErrorCode.CONSUMPTION_ALREADY_REFUNDED);
                        }
                        if (consumption.getStatus() == ConsumptionStatus.SETTLED
                                || consumption.getStatus() == ConsumptionStatus.REJECTED) {
                            log.warn("Consumo {} en estado {} - no es reembolsable",
                                    consumptionId, consumption.getStatus());
                            throw new BusinessException(ErrorCode.CONSUMPTION_NOT_REFUNDABLE);
                        }
                        log.debug("Consumo {} en estado {} - procediendo con reembolso",
                                consumptionId, consumption.getStatus());
                        consumption.setStatus(ConsumptionStatus.REFUNDED);
                        consumption.setUpdatedAt(LocalDateTime.now());
                        return consumptionPersistencePort.save(consumption)
                                .doOnSuccess(c -> {
                                    log.info("Consumo reembolsado exitosamente: {} de tarjeta {}, monto: {}",
                                            c.getId(), c.getCardId(), c.getAmount());
                                    cardCachePort.invalidateCardLimit(c.getCardId());
                                    publishConsumptionRefunded(c);
                                })
                                .doOnError(error ->
                                        log.error("Error al persistir reembolso del consumo {}: {}",
                                                consumptionId, error.getMessage()));
                    });
        } catch (BusinessException e) {
            log.error("Excepcion de negocio al reembolsar consumo {} en tarjeta {}: [{}] {}",
                    consumptionId, cardId, e.getErrorCode().name(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al reembolsar consumo {} en tarjeta {}: {}",
                    consumptionId, cardId, e.getMessage(), e);
            throw new RuntimeException("Error al reembolsar consumo: " + e.getMessage(), e);
        }
    }

    private void publishConsumptionRefunded(Consumption consumption) {
        ConsumptionRefundedEvent event = ConsumptionRefundedEvent.builder()
                .consumptionId(consumption.getId())
                .cardId(consumption.getCardId())
                .customerId(consumption.getCustomerId())
                .amount(consumption.getAmount())
                .occurredAt(LocalDateTime.now())
                .build();
        eventPublisherPort.publishConsumptionRefunded(event).subscribe(
                () -> log.debug("Evento ConsumptionRefunded publicado para consumo {}", consumption.getId()),
                error -> log.error("Error publicando ConsumptionRefunded: {}", error.getMessage())
        );
    }
}
