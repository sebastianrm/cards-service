package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.CustomerProfile;
import com.yanki.cards.domain.model.CustomerProfileView;
import com.yanki.cards.domain.model.CustomerType;
import com.yanki.cards.infrastructure.persistence.document.CustomerProfileViewDocument;

/**
 * Traductor estatico entre documentos MongoDB de vista de perfil y entidades de dominio.
 */
public final class CustomerProfileViewDocumentMapper {

    private CustomerProfileViewDocumentMapper() {
    }

    public static CustomerProfileView toDomain(CustomerProfileViewDocument document) {
        if (document == null) {
            return null;
        }
        return CustomerProfileView.builder()
                .customerId(document.getCustomerId())
                .customerType(stringToCustomerType(document.getCustomerType()))
                .profile(stringToCustomerProfile(document.getProfile()))
                .status(document.getStatus())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public static CustomerProfileViewDocument toDocument(CustomerProfileView view) {
        if (view == null) {
            return null;
        }
        return CustomerProfileViewDocument.builder()
                .customerId(view.getCustomerId())
                .customerType(view.getCustomerType() != null ? view.getCustomerType().name() : null)
                .profile(view.getProfile() != null ? view.getProfile().name() : null)
                .status(view.getStatus())
                .updatedAt(view.getUpdatedAt())
                .build();
    }

    private static CustomerType stringToCustomerType(String type) {
        if (type == null) {
            return null;
        }
        try {
            return CustomerType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static CustomerProfile stringToCustomerProfile(String profile) {
        if (profile == null) {
            return null;
        }
        try {
            return CustomerProfile.valueOf(profile);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
