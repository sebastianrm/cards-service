package com.yanki.cards.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento publicado cuando se registra un pago exitosamente.
 *
 * <p>Topic: {@code yanki.card.payment.registered.v1}
 * Key: cardId</p>
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
public class PaymentRegisteredEvent {

    /** Identificador de la tarjeta. */
    private String cardId;

    /** Identificador del cliente. */
    private String customerId;

    /** Monto del pago. */
    private BigDecimal amount;

    /** Limite disponible despues del pago. */
    private BigDecimal availableLimit;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
