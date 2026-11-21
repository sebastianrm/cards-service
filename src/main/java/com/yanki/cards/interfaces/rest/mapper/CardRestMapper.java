package com.yanki.cards.interfaces.rest.mapper;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.interfaces.rest.dto.CardResponse;

/**
 * Traductor estatico entre entidades de dominio y DTOs de respuesta REST.
 * <p>
 * Convierte entre Card (capa de dominio) y CardResponse (capa de interfaces REST).
 * </p>
 */
public final class CardRestMapper {

    private CardRestMapper() {
    }

    /**
     * Convierte una entidad de tarjeta a un DTO de respuesta REST.
     *
     * @param card entidad de dominio
     * @return DTO de respuesta
     */
    public static CardResponse toResponse(Card card) {
        if (card == null) {
            return null;
        }
        return new CardResponse(
                card.getId(),
                card.getCustomerId(),
                card.getType() != null ? card.getType().name() : null,
                card.getSubtype() != null ? card.getSubtype().name() : null,
                card.getStatus() != null ? card.getStatus().name() : null,
                card.getCardNumber(),
                card.getExpiryDate(),
                card.getCreditLimit(),
                card.getAvailableLimit(),
                card.getLinkedAccountId(),
                card.getIssuedAt(),
                card.getActivatedAt(),
                card.getCreatedAt()
        );
    }
}
