package com.yanki.cards.domain.model;

/**
 * Enum representing the type of customer in the system.
 * <p>
 * Distinguishes between individual and corporate customer types,
 * which may influence service eligibility and card issuance rules.
 * </p>
 * <ul>
 *   <li>INDIVIDUAL: Personal customer.</li>
 *   <li>CORPORATE: Business or company customer.</li>
 * </ul>
 * 
 * author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public enum CustomerType {
    INDIVIDUAL, CORPORATE
}
