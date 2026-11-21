package com.yanki.cards.domain.event;

/**
 * Constantes con los nombres de los topics Kafka para cards-service.
 *
 * <p>Centraliza la definicion de todos los topics de eventos que el servicio
 * produce y consume, evitando errores de tipeo y facilitando el mantenimiento.</p>
 *
 * <p>Topics producidos (outbound):</p>
 * <ul>
 *   <li>{@link #CARD_OPENED}: Emision exitosa de tarjeta</li>
 *   <li>{@link #CARD_CLOSED}: Cierre exitoso de tarjeta</li>
 *   <li>{@link #CARD_BLOCKED}: Bloqueo de tarjeta</li>
 *   <li>{@link #CARD_UNBLOCKED}: Desbloqueo de tarjeta</li>
 *   <li>{@link #CONSUMPTION_REGISTERED}: Registro de consumo</li>
 *   <li>{@link #CONSUMPTION_REFUNDED}: Reembolso de consumo</li>
 *   <li>{@link #PAYMENT_REGISTERED}: Registro de pago</li>
 * </ul>
 *
 * <p>Topics consumidos (inbound):</p>
 * <ul>
 *   <li>{@link #CUSTOMER_REGISTERED}: Registro de cliente</li>
 *   <li>{@link #CUSTOMER_UPDATED}: Actualizacion de cliente</li>
 *   <li>{@link #CUSTOMER_DELETED}: Eliminacion de cliente</li>
 *   <li>{@link #ACCOUNT_CLOSED}: Cierre de cuenta (para cerrar tarjetas debito vinculadas)</li>
 * </ul>
 *
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public final class Topics {

    // Producidos por cards-service
    public static final String CARD_OPENED = "yanki.card.opened.v1";
    public static final String CARD_CLOSED = "yanki.card.closed.v1";
    public static final String CARD_BLOCKED = "yanki.card.blocked.v1";
    public static final String CARD_UNBLOCKED = "yanki.card.unblocked.v1";
    public static final String CONSUMPTION_REGISTERED = "yanki.card.consumption.registered.v1";
    public static final String CONSUMPTION_REFUNDED = "yanki.card.consumption.refunded.v1";
    public static final String PAYMENT_REGISTERED = "yanki.card.payment.registered.v1";

    // Consumidos por cards-service
    public static final String CUSTOMER_REGISTERED = "yanki.customer.registered.v1";
    public static final String CUSTOMER_UPDATED = "yanki.customer.updated.v1";
    public static final String CUSTOMER_DELETED = "yanki.customer.deleted.v1";
    public static final String ACCOUNT_CLOSED = "yanki.account.closed.v1";

    private Topics() {
    }
}
