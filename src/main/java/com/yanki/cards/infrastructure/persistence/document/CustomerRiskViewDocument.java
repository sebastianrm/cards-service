package com.yanki.cards.infrastructure.persistence.document;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento MongoDB para la coleccion de vistas de riesgo de cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customer_risk_view")
public class CustomerRiskViewDocument {

    @Id
    private String customerId;

    private boolean hasOverdueDebt;
    private LocalDateTime updatedAt;
}
