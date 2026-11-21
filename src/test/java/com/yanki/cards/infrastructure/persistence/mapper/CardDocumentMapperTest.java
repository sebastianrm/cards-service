package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardStatus;
import com.yanki.cards.domain.model.CardSubtype;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.infrastructure.persistence.document.CardDocument;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del mapper de documentos de tarjeta entre capa de infraestructura y dominio.
 */
class CardDocumentMapperTest {

    @Test
    @DisplayName("toDomain - Convierte documento a entidad de dominio correctamente")
    void shouldConvertDocumentToDomain() {
        CardDocument document = CardDocument.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type("CREDIT")
                .subtype("PERSONAL")
                .status("ACTIVE")
                .cardNumber("4111111111111111")
                .expiryDate(LocalDateTime.now().plusYears(1))
                .cvvHash("hash123")
                .creditLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("10000"))
                .linkedAccountId("ACC-001")
                .createdAt(LocalDateTime.now())
                .build();

        Card card = CardDocumentMapper.toDomain(document);

        assertThat(card).isNotNull();
        assertThat(card.getId()).isEqualTo("CARD-001");
        assertThat(card.getCustomerId()).isEqualTo("CLI-1001");
        assertThat(card.getType()).isEqualTo(CardType.CREDIT);
        assertThat(card.getSubtype()).isEqualTo(CardSubtype.PERSONAL);
        assertThat(card.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(card.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(card.getCreditLimit()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("toDomain - Retorna null cuando el documento es null")
    void shouldReturnNullWhenDocumentIsNull() {
        assertThat(CardDocumentMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("toDomain - Maneja valor de enum invalido retornando null")
    void shouldHandleInvalidEnumValue() {
        CardDocument document = CardDocument.builder()
                .id("CARD-001")
                .type("INVALID_TYPE")
                .subtype("INVALID_SUBTYPE")
                .status("INVALID_STATUS")
                .build();

        Card card = CardDocumentMapper.toDomain(document);

        assertThat(card).isNotNull();
        assertThat(card.getType()).isNull();
        assertThat(card.getSubtype()).isNull();
        assertThat(card.getStatus()).isNull();
    }

    @Test
    @DisplayName("toDocument - Convierte entidad de dominio a documento correctamente")
    void shouldConvertDomainToDocument() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.CREDIT)
                .subtype(CardSubtype.PERSONAL)
                .status(CardStatus.ACTIVE)
                .cardNumber("4111111111111111")
                .creditLimit(new BigDecimal("10000"))
                .availableLimit(new BigDecimal("10000"))
                .build();

        CardDocument document = CardDocumentMapper.toDocument(card);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo("CARD-001");
        assertThat(document.getCustomerId()).isEqualTo("CLI-1001");
        assertThat(document.getType()).isEqualTo("CREDIT");
        assertThat(document.getSubtype()).isEqualTo("PERSONAL");
        assertThat(document.getStatus()).isEqualTo("ACTIVE");
        assertThat(document.getCardNumber()).isEqualTo("4111111111111111");
        assertThat(document.getCreditLimit()).isEqualTo(new BigDecimal("10000"));
    }

    @Test
    @DisplayName("toDocument - Retorna null cuando la entidad es null")
    void shouldReturnNullWhenCardIsNull() {
        assertThat(CardDocumentMapper.toDocument(null)).isNull();
    }

    @Test
    @DisplayName("toDocument - Maneja enum null sin lanzar excepcion")
    void shouldHandleNullEnum() {
        Card card = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(null)
                .subtype(null)
                .status(null)
                .build();

        CardDocument document = CardDocumentMapper.toDocument(card);

        assertThat(document).isNotNull();
        assertThat(document.getType()).isNull();
        assertThat(document.getSubtype()).isNull();
        assertThat(document.getStatus()).isNull();
    }

    @Test
    @DisplayName("toDomain y toDocument - Conversion round-trip preserva datos")
    void shouldPreserveDataInRoundTrip() {
        Card original = Card.builder()
                .id("CARD-001")
                .customerId("CLI-1001")
                .type(CardType.DEBIT)
                .subtype(CardSubtype.PERSONAL)
                .status(CardStatus.BLOCKED)
                .cardNumber("5222222222222222")
                .creditLimit(new BigDecimal("5000"))
                .availableLimit(new BigDecimal("3000"))
                .linkedAccountId("ACC-001")
                .blockReason("Pérdida")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();

        CardDocument document = CardDocumentMapper.toDocument(original);
        Card restored = CardDocumentMapper.toDomain(document);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getCustomerId()).isEqualTo(original.getCustomerId());
        assertThat(restored.getType()).isEqualTo(original.getType());
        assertThat(restored.getSubtype()).isEqualTo(original.getSubtype());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getCardNumber()).isEqualTo(original.getCardNumber());
        assertThat(restored.getCreditLimit()).isEqualTo(original.getCreditLimit());
        assertThat(restored.getAvailableLimit()).isEqualTo(original.getAvailableLimit());
        assertThat(restored.getBlockReason()).isEqualTo(original.getBlockReason());
    }
}
