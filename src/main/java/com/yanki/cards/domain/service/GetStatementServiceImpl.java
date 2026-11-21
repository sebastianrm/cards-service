package com.yanki.cards.domain.service;

import com.yanki.cards.domain.exception.BusinessException;
import com.yanki.cards.domain.exception.ErrorCode;
import com.yanki.cards.domain.model.Card;
import com.yanki.cards.domain.model.CardType;
import com.yanki.cards.domain.model.Consumption;
import com.yanki.cards.domain.port.in.GetStatementUseCase;
import com.yanki.cards.domain.port.out.CardPersistencePort;
import com.yanki.cards.domain.port.out.ConsumptionPersistencePort;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para obtener el estado de cuenta mensual de una tarjeta de crédito.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetStatementServiceImpl implements GetStatementUseCase {

    private final CardPersistencePort cardPersistencePort;
    private final ConsumptionPersistencePort consumptionPersistencePort;

    @Override
    public Flowable<StatementResult> execute(String cardId, String month) {
        log.debug("Consultando estado de cuenta - tarjeta: {}, mes: {}", cardId, month);

        try {
            if (month == null || month.isEmpty()) {
                log.warn("Mes nulo o vacio para estado de cuenta de tarjeta {}", cardId);
                return Flowable.error(new BusinessException(ErrorCode.INVALID_FILTERS));
            }

            LocalDate statementMonth;
            try {
                statementMonth = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                log.warn("Formato de mes invalido: {} para estado de cuenta de tarjeta {}", month, cardId);
                return Flowable.error(new BusinessException(ErrorCode.INVALID_FILTERS));
            }

            return cardPersistencePort.findById(cardId)
                    .switchIfEmpty(Single.error(() -> {
                        log.warn("Tarjeta {} no encontrada para estado de cuenta del mes {}", cardId, month);
                        return new BusinessException(ErrorCode.CARD_NOT_FOUND);
                    }))
                    .toFlowable()
                    .flatMap(card -> {
                        if (card.getType() != CardType.CREDIT) {
                            log.warn("Tarjeta {} no es de credito (tipo: {}) - estado de cuenta no aplicable",
                                    cardId, card.getType());
                            return Flowable.error(new BusinessException(ErrorCode.INVALID_CARD_TYPE));
                        }
                        log.debug("Obteniendo consumos autorizados para tarjeta {} - mes: {}", cardId, month);
                        return consumptionPersistencePort.findByCardIdAndStatus(cardId, "AUTHORIZED")
                                .toList()
                                .toFlowable()
                                .map(consumptions -> {
                                    BigDecimal total = consumptions.stream()
                                            .map(Consumption::getAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    log.info("Estado de cuenta consultado exitosamente: tarjeta={}, mes={}, consumos={}, total={}",
                                            cardId, month, consumptions.size(), total);
                                    return new StatementResult(
                                            cardId,
                                            statementMonth,
                                            consumptions,
                                            total,
                                            card.getAvailableLimit()
                                    );
                                });
                    })
                    .doOnError(error ->
                            log.error("Error al consultar estado de cuenta de tarjeta {} - mes {}: {}",
                                    cardId, month, error.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al consultar estado de cuenta de tarjeta {} - mes {}: {}",
                    cardId, month, e.getMessage(), e);
            throw new RuntimeException("Error al consultar estado de cuenta: " + e.getMessage(), e);
        }
    }
}
