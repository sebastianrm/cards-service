package com.yanki.cards.domain.port.in;

import com.yanki.cards.domain.model.Card;
import io.reactivex.rxjava3.core.Single;

/**
 * Puerto de entrada para emitir una nueva tarjeta de credito o debito.
 * <p>
 * Valida la elegibilidad del cliente, el riesgo de deuda vencida,
 * la duplicidad de tarjetas y calcula los limites aplicables.
 * </p>
 */
public interface IssueCardUseCase {

    /**
     * Emite una nueva tarjeta para el cliente indicado.
     *
     * @param customerId identificador del cliente
     * @param type tipo de tarjeta (CREDIT o DEBIT)
     * @param subtype subtipo de tarjeta (PERSONAL o ENTERPRISE)
     * @param creditLimit limite de credito para tarjetas de credito
     * @param linkedAccountId cuenta vinculada (obligatorio para debito)
     * @return tarjeta emitida
     */
    Single<Card> execute(String customerId, String type, String subtype, java.math.BigDecimal creditLimit, String linkedAccountId);
}
