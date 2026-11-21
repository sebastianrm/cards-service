package com.yanki.cards.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento publicado cuando se desbloquea una tarjeta.
 *
 * <p>Topic: {@code yanki.card.unblocked.v1}
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
public class CardUnblockedEvent {

    /** Identificador de la tarjeta. */
    private String cardId;

    /** Identificador del cliente. */
    private String customerId;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
