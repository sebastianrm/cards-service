package com.yanki.cards.domain.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Evento recibido de customers-service cuando se registra un nuevo cliente.
 *
 * <p>Topic: {@code yanki.customer.registered.v1}
 * Key: customerId</p>
 *
 * <p>cards-service consume este evento para actualizar la vista local
 * customer_profile_view y mantener la proyeccion sincronizada.</p>
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
public class CustomerRegisteredEvent {

    /** Identificador del cliente. */
    private String customerId;

    /** Tipo de cliente: PERSONAL, CORPORATE. */
    private String customerType;

    /** Nombre del cliente. */
    private String fullName;

    /** Estado del cliente: ACTIVE, INACTIVE, SUSPENDED. */
    private String status;

    /** Fecha del evento. */
    private LocalDateTime occurredAt;
}
