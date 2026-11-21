package com.yanki.cards.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento publicado cuando se emite una tarjeta exitosamente.
 *
 * <p>Topic: {@code yanki.card.opened.v1}
 * Key: cardId</p>
 *
 * <p>Se publica tras la creacion y persistencia de la tarjeta en MongoDB,
 * notificando a otros servicios (reports, notifications) sobre la nueva tarjeta.</p>
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
public class CardOpenedEvent {

    /** Identificador de la tarjeta. */
    private String cardId;

    /** Identificador del cliente. */
    private String customerId;

    /** Tipo de tarjeta: CREDIT, DEBIT. */
    private String cardType;

    /** Subtipo de tarjeta: PERSONAL, CORPORATE. */
    private String cardSubtype;

    /** Limite de credito asignado. */
    private BigDecimal creditLimit;

    /** Cuenta vinculada (para debito). */
    private String linkedAccountId;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
