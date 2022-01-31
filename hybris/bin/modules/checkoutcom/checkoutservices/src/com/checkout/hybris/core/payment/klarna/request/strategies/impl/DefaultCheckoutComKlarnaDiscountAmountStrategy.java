package com.checkout.hybris.core.payment.klarna.request.strategies.impl;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.klarna.session.request.KlarnaProductRequestDto;
import com.checkout.hybris.core.payment.klarna.request.strategies.CheckoutComKlarnaDiscountAmountStrategy;
import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.CartModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation does nothing. SI need to implement the custom logic for discounts
 */
public class DefaultCheckoutComKlarnaDiscountAmountStrategy implements CheckoutComKlarnaDiscountAmountStrategy {

    protected final CheckoutComCurrencyService checkoutComCurrencyService;

    public DefaultCheckoutComKlarnaDiscountAmountStrategy(final CheckoutComCurrencyService checkoutComCurrencyService) {
        this.checkoutComCurrencyService = checkoutComCurrencyService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyDiscountsToKlarnaOrderLines(final CartModel cart, final List<KlarnaProductRequestDto> productLines) {
        validateParameterNotNull(cart, "CartModel cannot be null.");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(cart.getEntries()), "List of cart entries cannot be empty.");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(productLines), "List of KlarnaProductRequestDto cannot be empty.");
        final String currencyCode = cart.getCurrency() != null ? cart.getCurrency().getIsocode() : null;

        final AtomicLong totalDiscounts = new AtomicLong(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, cart.getTotalDiscounts()));

        if (NumberUtils.LONG_ZERO.equals(totalDiscounts.get())) {
            return;
        }
        // Implements the SI logic for klarna discounts
    }
}
