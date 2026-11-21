package com.yanki.cards.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad pura de dominio que representa un evento ya procesado por el servicio
 * para el control de idempotencia.
 * <p>
 * Almacena el topic del evento, el identificador de la entidad relacionada
 * y las marcas de tiempo que indican cuando ocurrio y cuando se proceso el evento.
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
public class ProcessedEvent {

    private String id;
    private String topic;
    private String entityId;
    private LocalDateTime occurredAt;
    private LocalDateTime processedAt;
}
