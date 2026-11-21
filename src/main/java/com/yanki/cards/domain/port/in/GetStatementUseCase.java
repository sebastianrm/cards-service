package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Consumption;
import io.reactivex.rxjava3.core.Flowable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Puerto de entrada para obtener el estado de cuenta mensual de una tarjeta de crédito.
 */
public interface GetStatementUseCase {

    /**
     * Resultado del estado de cuenta mensual.
     */
    record StatementResult(
            String cardId,
            LocalDate month,
            java.util.List<Consumption> consumptions,
            BigDecimal totalAmount,
            BigDecimal availableLimit
    ) {}

    /**
     * Obtiene el estado de cuenta mensual de una tarjeta de crédito.
     *
     * @param cardId identificador de la tarjeta
     * @param month mes del estado de cuenta (YYYY-MM)
     * @return resultado del estado de cuenta
     */
    Flowable<StatementResult> execute(String cardId, String month);
}
