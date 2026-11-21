package com.yanki.cards.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad pura de dominio que representa una tarjeta de pago asociada a un cliente
 * dentro de la plataforma bancaria Yanki.
 * <p>
 * Contiene metadatos como tipo, subtipo, estado, limites de credito y disponibles,
 * y marcas de tiempo importantes del ciclo de vida de la tarjeta.
 * </p>
 *
 * @author Sebastian Retamal &lt;semorale@gmail.com&gt;
 * @version 1.0
 * @since 2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    private String id;
    private String customerId;
    private CardType type;
    private CardSubtype subtype;
    private CardStatus status;
    private String cardNumber;
    private LocalDateTime expiryDate;
    private String cvvHash;
    private BigDecimal creditLimit;
    private BigDecimal availableLimit;
    private String linkedAccountId;
    private LocalDateTime issuedAt;
    private LocalDateTime activatedAt;
    private LocalDateTime blockedAt;
    private String blockReason;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
