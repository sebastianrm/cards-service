package com.yanki.cards.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad pura de dominio que representa la vista local del perfil de riesgo de un cliente
 * relevante para la evaluacion de credito.
 * <p>
 * Indica si el cliente tiene deudas vencidas. Esta vista ayuda en la evaluacion
 * de solvencia crediticia y la gestion de riesgos.
 * </p>
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
public class CustomerRiskView {

    private String customerId;
    private boolean hasOverdueDebt;
    private LocalDateTime updatedAt;
}
