package com.yanki.cards.domain.model;

/**
 * Enum representing the types of cards supported by the system.
 * <p>
 * Includes credit and debit categories which determine card behavior
 * and processing rules.
 * </p>
 * <ul>
 *   <li>CREDIT: Card enabling credit transactions with associated credit limits.</li>
 *   <li>DEBIT: Card linked to an existing account for direct debit transactions.</li>
 * </ul>
 * 
 * author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public enum CardType {
    CREDIT, DEBIT
}
