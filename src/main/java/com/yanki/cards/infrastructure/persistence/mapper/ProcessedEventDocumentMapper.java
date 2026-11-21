package com.yanki.cards.infrastructure.persistence.mapper;

import com.yanki.cards.domain.model.ProcessedEvent;
import com.yanki.cards.infrastructure.persistence.document.ProcessedEventDocument;

/**
 * Traductor estatico entre documentos MongoDB de eventos procesados y entidades de dominio.
 */
public final class ProcessedEventDocumentMapper {

    private ProcessedEventDocumentMapper() {
    }

    public static ProcessedEvent toDomain(ProcessedEventDocument document) {
        if (document == null) {
            return null;
        }
        return ProcessedEvent.builder()
                .id(document.getId())
                .topic(document.getTopic())
                .entityId(document.getEntityId())
                .occurredAt(document.getOccurredAt())
                .processedAt(document.getProcessedAt())
                .build();
    }

    public static ProcessedEventDocument toDocument(ProcessedEvent event) {
        if (event == null) {
            return null;
        }
        return ProcessedEventDocument.builder()
                .id(event.getId())
                .topic(event.getTopic())
                .entityId(event.getEntityId())
                .occurredAt(event.getOccurredAt())
                .processedAt(event.getProcessedAt())
                .build();
    }
}
