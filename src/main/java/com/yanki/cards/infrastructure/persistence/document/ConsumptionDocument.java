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
 * Documento MongoDB para la coleccion de consumos.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consumptions")
public class ConsumptionDocument {

    @Id
    private String id;

    private String cardId;
    private String customerId;
    private BigDecimal amount;
    private String merchantId;
    private String description;
    private String status;
    private String paymentId;
    private LocalDateTime occurredAt;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
