package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.infrastructure.persistence.document.ConsumptionDocument;

/**
 * Traductor estatico entre documentos MongoDB de consumo y entidades de dominio.
 */
public final class ConsumptionDocumentMapper {

    private ConsumptionDocumentMapper() {
    }

    public static Consumption toDomain(ConsumptionDocument document) {
        if (document == null) {
            return null;
        }
        return Consumption.builder()
                .id(document.getId())
                .cardId(document.getCardId())
                .customerId(document.getCustomerId())
                .amount(document.getAmount())
                .merchantId(document.getMerchantId())
                .description(document.getDescription())
                .status(stringToStatus(document.getStatus()))
                .paymentId(document.getPaymentId())
                .occurredAt(document.getOccurredAt())
                .settledAt(document.getSettledAt())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public static ConsumptionDocument toDocument(Consumption consumption) {
        if (consumption == null) {
            return null;
        }
        return ConsumptionDocument.builder()
                .id(consumption.getId())
                .cardId(consumption.getCardId())
                .customerId(consumption.getCustomerId())
                .amount(consumption.getAmount())
                .merchantId(consumption.getMerchantId())
                .description(consumption.getDescription())
                .status(consumption.getStatus() != null ? consumption.getStatus().name() : null)
                .paymentId(consumption.getPaymentId())
                .occurredAt(consumption.getOccurredAt())
                .settledAt(consumption.getSettledAt())
                .createdAt(consumption.getCreatedAt())
                .updatedAt(consumption.getUpdatedAt())
                .build();
    }

    private static ConsumptionStatus stringToStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return ConsumptionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
