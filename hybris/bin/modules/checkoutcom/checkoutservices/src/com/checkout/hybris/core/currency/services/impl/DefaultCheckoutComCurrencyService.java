package com.checkout.hybris.core.currency.services.impl;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Default implementation of the {@link CheckoutComCurrencyService}
 */
public class DefaultCheckoutComCurrencyService implements CheckoutComCurrencyService {

    protected final CommonI18NService commonI18NService;

    public DefaultCheckoutComCurrencyService(final CommonI18NService commonI18NService) {
        this.commonI18NService = commonI18NService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long convertAmountIntoPennies(final String currencyIsoCode, final Double amount) {
        validateParameterNotNull(currencyIsoCode, "Currency code cannot be null");
        validateParameterNotNull(amount, "amount cannot be null");

        final Currency currency = Currency.getInstance(currencyIsoCode);
        final Double roundedValue = commonI18NService.convertAndRoundCurrency(1, Math.pow(10, currency.getDefaultFractionDigits()), 0, amount);
        return roundedValue.longValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal convertAmountFromPennies(final String currencyIsoCode, final Long amountInPennies) {
        validateParameterNotNull(currencyIsoCode, "Currency code cannot be null");
        validateParameterNotNull(amountInPennies, "amount cannot be null");

        final Currency currency = Currency.getInstance(currencyIsoCode);
        return new BigDecimal(amountInPennies).movePointLeft(currency.getDefaultFractionDigits()).setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }
}
