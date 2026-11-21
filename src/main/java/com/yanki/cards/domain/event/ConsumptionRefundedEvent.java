package com.yanki.cards.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento publicado cuando se reembolsa un consumo exitosamente.
 *
 * <p>Topic: {@code yanki.card.consumption.refunded.v1}
 * Key: consumptionId</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionRefundedEvent {

    /** Identificador del consumo. */
    private String consumptionId;

    /** Identificador de la tarjeta. */
    private String cardId;

    /** Identificador del cliente. */
    private String customerId;

    /** Monto reembolsado. */
    private BigDecimal amount;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
