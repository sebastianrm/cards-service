package com.yanki.cards.infrastructure.persistence.document;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento MongoDB para la coleccion de eventos procesados (control de idempotencia).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processed_events")
public class ProcessedEventDocument {

    @Id
    private String id;

    private String topic;
    private String entityId;
    private LocalDateTime occurredAt;
    private LocalDateTime processedAt;
}
