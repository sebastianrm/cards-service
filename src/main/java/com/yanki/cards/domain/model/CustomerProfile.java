package com.yanki.cards.domain.model;

/**
 * Enum representing the profile category of a customer.
 * <p>
 * Defines the customer's profile as BASIC, VIP, or PYME, affecting
 * eligibility and service offerings.
 * </p>
 * <ul>
 *   <li>BASIC: Standard customer profile with basic access.</li>
 *   <li>VIP: Premium customer profile with advanced benefits.</li>
 *   <li>PYME: Small and medium enterprise profile for business customers.</li>
 * </ul>
 * 
 * author Sebastian Retamal
 * @version 1.0
 * @since 2026
 */
public enum CustomerProfile {
    BASIC, VIP, PYME
}
