package com.yanki.cards.domain.model;

/**
 * Enum representing the status of a consumption transaction.
 * <p>
 * Indicates the current state of a transaction and its processing stage.
 * </p>
 * <ul>
 *   <li>AUTHORIZED: Transaction has been approved but not completed.</li>
 *   <li>CONFIRMED: Transaction has been confirmed and is awaiting settlement.</li>
 *   <li>SETTLED: Transaction has been finalized and settled in the system.</li>
 *   <li>REJECTED: Transaction has been declined and will not be processed.</li>
 *   <li>REFUNDED: Transaction has been returned to the originator after settlement.</li>
 * </ul>
 * 
 * author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public enum ConsumptionStatus {
    AUTHORIZED, CONFIRMED, SETTLED, REJECTED, REFUNDED
}
