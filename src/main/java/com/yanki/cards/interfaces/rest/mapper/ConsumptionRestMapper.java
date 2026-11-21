package com.yanki.cards.interfaces.rest.mapper;

import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.interfaces.rest.dto.ConsumptionResponse;

/**
 * Mapper estático entre entidades de dominio Consumption y DTOs REST.
 */
public final class ConsumptionRestMapper {

    private ConsumptionRestMapper() {
        throw new UnsupportedOperationException("Clase de utilidad, no se puede instanciar");
    }

    /**
     * Convierte una entidad Consumption a ConsumptionResponse.
     */
    public static ConsumptionResponse toResponse(Consumption consumption) {
        return new ConsumptionResponse(
                consumption.getId(),
                consumption.getCardId(),
                consumption.getCustomerId(),
                consumption.getAmount(),
                consumption.getMerchantId(),
                consumption.getDescription(),
                consumption.getStatus() != null ? consumption.getStatus().name() : null,
                consumption.getPaymentId(),
                consumption.getOccurredAt(),
                consumption.getSettledAt(),
                consumption.getCreatedAt(),
                consumption.getUpdatedAt()
        );
    }
}
