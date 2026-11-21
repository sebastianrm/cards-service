package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.model.ConsumptionStatus;
import com.yanki.cards.infrastructure.persistence.document.ConsumptionDocument;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del mapper de documentos de consumo entre capa de infraestructura y dominio.
 */
class ConsumptionDocumentMapperTest {

    @Test
    @DisplayName("toDomain - Convierte documento a entidad de dominio correctamente")
    void shouldConvertDocumentToDomain() {
        ConsumptionDocument document = ConsumptionDocument.builder()
                .id("CONS-001")
                .cardId("CARD-001")
                .customerId("CLI-1001")
                .amount(new BigDecimal("150.50"))
                .merchantId("MERCHANT-001")
                .description("Compra supermercado")
                .status("AUTHORIZED")
                .occurredAt(LocalDateTime.now())
                .build();

        Consumption consumption = ConsumptionDocumentMapper.toDomain(document);

        assertThat(consumption).isNotNull();
        assertThat(consumption.getId()).isEqualTo("CONS-001");
        assertThat(consumption.getCardId()).isEqualTo("CARD-001");
        assertThat(consumption.getAmount()).isEqualTo(new BigDecimal("150.50"));
        assertThat(consumption.getStatus()).isEqualTo(ConsumptionStatus.AUTHORIZED);
        assertThat(consumption.getMerchantId()).isEqualTo("MERCHANT-001");
    }

    @Test
    @DisplayName("toDomain - Retorna null cuando el documento es null")
    void shouldReturnNullWhenDocumentIsNull() {
        assertThat(ConsumptionDocumentMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("toDomain - Maneja valor de estado invalido")
    void shouldHandleInvalidStatusValue() {
        ConsumptionDocument document = ConsumptionDocument.builder()
                .id("CONS-001")
                .status("INVALID_STATUS")
                .build();

        Consumption consumption = ConsumptionDocumentMapper.toDomain(document);

        assertThat(consumption).isNotNull();
        assertThat(consumption.getStatus()).isNull();
    }

    @Test
    @DisplayName("toDocument - Convierte entidad de dominio a documento correctamente")
    void shouldConvertDomainToDocument() {
        Consumption consumption = Consumption.builder()
                .id("CONS-001")
                .cardId("CARD-001")
                .customerId("CLI-1001")
                .amount(new BigDecimal("150.50"))
                .merchantId("MERCHANT-001")
                .status(ConsumptionStatus.SETTLED)
                .build();

        ConsumptionDocument document = ConsumptionDocumentMapper.toDocument(consumption);

        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo("CONS-001");
        assertThat(document.getCardId()).isEqualTo("CARD-001");
        assertThat(document.getAmount()).isEqualTo(new BigDecimal("150.50"));
        assertThat(document.getStatus()).isEqualTo("SETTLED");
    }

    @Test
    @DisplayName("toDocument - Retorna null cuando la entidad es null")
    void shouldReturnNullWhenConsumptionIsNull() {
        assertThat(ConsumptionDocumentMapper.toDocument(null)).isNull();
    }

    @Test
    @DisplayName("toDocument - Maneja status null sin lanzar excepcion")
    void shouldHandleNullStatus() {
        Consumption consumption = Consumption.builder()
                .id("CONS-001")
                .status(null)
                .build();

        ConsumptionDocument document = ConsumptionDocumentMapper.toDocument(consumption);

        assertThat(document).isNotNull();
        assertThat(document.getStatus()).isNull();
    }

    @Test
    @DisplayName("Round-trip - Preserva datos en conversion bidireccional")
    void shouldPreserveDataInRoundTrip() {
        Consumption original = Consumption.builder()
                .id("CONS-001")
                .cardId("CARD-001")
                .customerId("CLI-1001")
                .amount(new BigDecimal("250.75"))
                .merchantId("MERCHANT-001")
                .description("Restaurante")
                .status(ConsumptionStatus.REFUNDED)
                .paymentId("PAY-001")
                .occurredAt(LocalDateTime.of(2026, 11, 10, 15, 30))
                .build();

        ConsumptionDocument document = ConsumptionDocumentMapper.toDocument(original);
        Consumption restored = ConsumptionDocumentMapper.toDomain(document);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getCardId()).isEqualTo(original.getCardId());
        assertThat(restored.getAmount()).isEqualTo(original.getAmount());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getMerchantId()).isEqualTo(original.getMerchantId());
        assertThat(restored.getDescription()).isEqualTo(original.getDescription());
        assertThat(restored.getPaymentId()).isEqualTo(original.getPaymentId());
    }
}
