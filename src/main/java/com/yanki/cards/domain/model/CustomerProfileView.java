package com.yanki.cards.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad pura de dominio que representa la vista local del perfil de un cliente
 * utilizada para la toma de decisiones de emision de tarjetas.
 * <p>
 * Contiene el tipo de cliente, perfil, estado y la ultima marca de tiempo de actualizacion.
 * Esta vista agrega datos relevantes para las verificaciones de elegibilidad del cliente.
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
public class CustomerProfileView {

    private String customerId;
    private CustomerType customerType;
    private CustomerProfile profile;
    private String status;
    private LocalDateTime updatedAt;
}
