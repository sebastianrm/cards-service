package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardSubtype;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.infrastructure.persistence.document.CardDocument;

/**
 * Traductor estatico entre documentos MongoDB de tarjeta y entidades de dominio.
 * <p>
 * Convierte entre CardDocument (capa de infraestructura) y Card (capa de dominio),
 * mapeando los enums y campos correspondientes.
 * </p>
 */
public final class CardDocumentMapper {

    private CardDocumentMapper() {
        // Clase utilitaria - no se instancia
    }

    /**
     * Convierte un documento de tarjeta a una entidad de dominio.
     *
     * @param document documento MongoDB
     * @return entidad de dominio
     */
    public static Card toDomain(CardDocument document) {
        if (document == null) {
            return null;
        }
        return Card.builder()
                .id(document.getId())
                .customerId(document.getCustomerId())
                .type(stringToCardType(document.getType()))
                .subtype(stringToCardSubtype(document.getSubtype()))
                .status(stringToCardStatus(document.getStatus()))
                .cardNumber(document.getCardNumber())
                .expiryDate(document.getExpiryDate())
                .cvvHash(document.getCvvHash())
                .creditLimit(document.getCreditLimit())
                .availableLimit(document.getAvailableLimit())
                .linkedAccountId(document.getLinkedAccountId())
                .issuedAt(document.getIssuedAt())
                .activatedAt(document.getActivatedAt())
                .blockedAt(document.getBlockedAt())
                .blockReason(document.getBlockReason())
                .closedAt(document.getClosedAt())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    /**
     * Convierte una entidad de dominio a un documento de tarjeta.
     *
     * @param card entidad de dominio
     * @return documento MongoDB
     */
    public static CardDocument toDocument(Card card) {
        if (card == null) {
            return null;
        }
        return CardDocument.builder()
                .id(card.getId())
                .customerId(card.getCustomerId())
                .type(card.getType() != null ? card.getType().name() : null)
                .subtype(card.getSubtype() != null ? card.getSubtype().name() : null)
                .status(card.getStatus() != null ? card.getStatus().name() : null)
                .cardNumber(card.getCardNumber())
                .expiryDate(card.getExpiryDate())
                .cvvHash(card.getCvvHash())
                .creditLimit(card.getCreditLimit())
                .availableLimit(card.getAvailableLimit())
                .linkedAccountId(card.getLinkedAccountId())
                .issuedAt(card.getIssuedAt())
                .activatedAt(card.getActivatedAt())
                .blockedAt(card.getBlockedAt())
                .blockReason(card.getBlockReason())
                .closedAt(card.getClosedAt())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }

    private static CardType stringToCardType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return CardType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static CardSubtype stringToCardSubtype(String subtype) {
        if (subtype == null) {
            return null;
        }
        try {
            return CardSubtype.valueOf(subtype);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static CardStatus stringToCardStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return CardStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
