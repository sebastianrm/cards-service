package com.yanki.cards.domain.model;

/**
 * Enum representing possible subtypes of a card within the system.
 * <p>
 * Defines categories of cards such as personal or enterprise, which influence applicable limits and features.
 * </p>
 * <ul>
 *   <li>PERSONAL: A card issued for personal and individual use.</li>
 *   <li>ENTERPRISE: A card designed for business-related expenses and management.</li>
 * </ul>
 * 
 * author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public enum CardSubtype {
    PERSONAL, ENTERPRISE
}
