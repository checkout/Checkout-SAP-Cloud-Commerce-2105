package com.checkout.hybris.core.payment.details.strategies.impl;

import de.hybris.platform.order.CartService;

/**
 * Implements the default logic to update the payment info on payment success response
 */
public class DefaultCheckoutComUpdatePaymentInfoStrategy extends CheckoutComAbstractUpdatePaymentInfoStrategy {

    public DefaultCheckoutComUpdatePaymentInfoStrategy(final CartService cartService) {
        super(cartService);
    }
}