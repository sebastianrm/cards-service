package com.yanki.cards.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento recibido de accounts-service cuando se cierra una cuenta.
 *
 * <p>Topic: {@code yanki.account.closed.v1}
 * Key: accountId</p>
 *
 * <p>cards-service consume este evento para cerrar las tarjetas de debito
 * vinculadas a la cuenta cerrada.</p>
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
public class AccountClosedEvent {

    /** Identificador de la cuenta. */
    private String accountId;

    /** Identificador del cliente. */
    private String customerId;

    /** Motivo del cierre. */
    private String reason;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
