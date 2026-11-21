package com.yanki.cards.infrastructure.messaging.consumer;

import com.yanki.cards.domain.event.AccountClosedEvent;
import com.yanki.cards.domain.event.Topics;
import com.yanki.cards.domain.model.ProcessedEvent;
import com.yanki.cards.domain.port.in.HandleAccountClosedUseCase;
import com.yanki.cards.domain.port.out.ProcessedEventPersistencePort;
import io.reactivex.rxjava3.core.Completable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka idempotente para eventos de cierre de cuenta.
 *
 * <p>Antes de procesar cada evento, verifica en la coleccion local
 * processed_events si ya fue procesado, garantizando idempotencia
 * conforme a la Regla de Oro de la plataforma Yanki.</p>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountClosedConsumer {

    private final HandleAccountClosedUseCase handleAccountClosedUseCase;
    private final ProcessedEventPersistencePort processedEventPort;

    @KafkaListener(topics = Topics.ACCOUNT_CLOSED, groupId = "${spring.kafka.consumer.group-id:cards-service}",
            containerFactory = "accountClosedKafkaListenerContainerFactory")
    public void onAccountClosed(AccountClosedEvent event) {
        String idempotencyKey = Topics.ACCOUNT_CLOSED + ":" + event.getAccountId() + ":" + event.getOccurredAt();

        log.info("Evento {} recibido para cuenta {}", Topics.ACCOUNT_CLOSED, event.getAccountId());

        processedEventPort.existsByTopicAndEntityId(Topics.ACCOUNT_CLOSED, event.getAccountId())
                .flatMapCompletable(exists -> {
            if (exists) {
                log.info("Evento {} ya procesado para cuenta {}, ignorando", Topics.ACCOUNT_CLOSED, event.getAccountId());
                return Completable.complete();
            }
            return handleAccountClosedUseCase.execute(event)
                    .andThen(saveProcessedEvent(idempotencyKey, event.getAccountId(), event.getOccurredAt()));
        }).subscribe(
                () -> log.info("Evento {} procesado para cuenta {}", Topics.ACCOUNT_CLOSED, event.getAccountId()),
                error -> log.error("Error procesando {}: {}", Topics.ACCOUNT_CLOSED, error.getMessage())
        );
    }

    private Completable saveProcessedEvent(String id, String entityId, LocalDateTime occurredAt) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .id(id)
                .topic(Topics.ACCOUNT_CLOSED)
                .entityId(entityId)
                .occurredAt(occurredAt)
                .processedAt(LocalDateTime.now())
                .build();
        return processedEventPort.save(processedEvent).ignoreElement();
    }
}
