package com.checkout.hybris.core.currency.services;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Handle operations about currency and conversions
 */
public interface CheckoutComCurrencyService {

    /**
     * Creates an amount in pennies value using the {@link Currency} and the amount.
     *
     * @param currencyIsoCode the current currency code
     * @param amount          the value amount
     * @return the amount in pennies
     */
    Long convertAmountIntoPennies(String currencyIsoCode, Double amount);

    /**
     * Converts an amount in BigDecimal value taking into account the currency
     *
     * @param currencyIsoCode the current currency code
     * @param amountInPennies the amount in pennies
     * @return the amount from pennies
     */
    BigDecimal convertAmountFromPennies(String currencyIsoCode, Long amountInPennies);
}
