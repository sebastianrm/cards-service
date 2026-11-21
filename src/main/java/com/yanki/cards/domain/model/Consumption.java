package com.yanki.cards.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad pura de dominio que representa un consumo o transaccion de pago registrada en el sistema.
 * <p>
 * Almacena el monto de la transaccion, informacion del comercio, asociacion de pago,
 * estado y marcas de tiempo de auditoria del ciclo de vida del consumo.
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
public class Consumption {

    private String id;
    private String cardId;
    private String customerId;
    private BigDecimal amount;
    private String merchantId;
    private String description;
    private ConsumptionStatus status;
    private String paymentId;
    private LocalDateTime occurredAt;
    private LocalDateTime settledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
