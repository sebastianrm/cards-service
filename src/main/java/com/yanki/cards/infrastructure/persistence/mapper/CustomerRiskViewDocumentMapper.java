package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.CustomerRiskView;
import com.yanki.cards.infrastructure.persistence.document.CustomerRiskViewDocument;

/**
 * Traductor estatico entre documentos MongoDB de vista de riesgo y entidades de dominio.
 */
public final class CustomerRiskViewDocumentMapper {

    private CustomerRiskViewDocumentMapper() {
    }

    public static CustomerRiskView toDomain(CustomerRiskViewDocument document) {
        if (document == null) {
            return null;
        }
        return CustomerRiskView.builder()
                .customerId(document.getCustomerId())
                .hasOverdueDebt(document.isHasOverdueDebt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public static CustomerRiskViewDocument toDocument(CustomerRiskView view) {
        if (view == null) {
            return null;
        }
        return CustomerRiskViewDocument.builder()
                .customerId(view.getCustomerId())
                .hasOverdueDebt(view.isHasOverdueDebt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }
}
