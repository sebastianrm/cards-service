package com.yanki.cards.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento publicado cuando se cierra una tarjeta exitosamente.
 *
 * <p>Topic: {@code yanki.card.closed.v1}
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
public class CardClosedEvent {

    /** Identificador de la tarjeta. */
    private String cardId;

    /** Identificador del cliente. */
    private String customerId;

    /** Tipo de tarjeta: CREDIT, DEBIT. */
    private String cardType;

    /** Motivo del cierre. */
    private String reason;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
