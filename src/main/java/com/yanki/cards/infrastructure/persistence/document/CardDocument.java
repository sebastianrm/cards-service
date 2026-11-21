package com.yanki.cards.infrastructure.persistence.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento MongoDB para la coleccion de tarjetas.
 * <p>
 * Mapea la entidad de dominio Card a la estructura de almacenamiento
 * con anotaciones especificas de Spring Data MongoDB.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cards")
public class CardDocument {

    @Id
    private String id;

    private String customerId;
    private String type;
    private String subtype;
    private String status;
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
