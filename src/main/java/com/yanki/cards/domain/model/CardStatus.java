package com.yanki.cards.domain.model;

/**
 * Enum representing the lifecycle status of a card in the system.
 * <p>
 * Determines the current state of a card, influencing available actions and transactions.
 * </p>
 * <ul>
 *   <li>ISSUED: The card has been created but not yet activated.</li>
 *   <li>ACTIVE: The card is currently active and usable for transactions.</li>
 *   <li>BLOCKED: The card is temporarily blocked from usage due to specific reasons.</li>
 *   <li>EXPIRED: The card has reached its expiry without renewal actions.</li>
 *   <li>CLOSED: The card has been closed and is no longer valid.</li>
 * </ul>
 * 
 * @author Sebastian Retamal
 * @version 1.0
 * @since 2026
 * @see Card
 */
public enum CardStatus {
    ISSUED, ACTIVE, BLOCKED, EXPIRED, CLOSED
}
