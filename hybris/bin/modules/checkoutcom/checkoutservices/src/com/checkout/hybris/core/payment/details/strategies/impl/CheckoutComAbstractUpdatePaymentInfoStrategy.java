package com.checkout.hybris.core.payment.details.strategies.impl;

import com.checkout.hybris.core.payment.details.strategies.CheckoutComUpdatePaymentInfoStrategy;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Abstract implementation of {@link CheckoutComUpdatePaymentInfoStrategy}
 */
public abstract class CheckoutComAbstractUpdatePaymentInfoStrategy implements CheckoutComUpdatePaymentInfoStrategy {

    protected CartService cartService;

    public CheckoutComAbstractUpdatePaymentInfoStrategy(final CartService cartService) {
        this.cartService = cartService;
    }

    protected CheckoutComAbstractUpdatePaymentInfoStrategy() {
        // default empty constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPaymentResponse(final GetPaymentResponse paymentResponse) {
        checkArgument(paymentResponse != null, "Payment id cannot be empty.");
        if (!cartService.hasSessionCart()) {
            throw new IllegalArgumentException("The current Session does not have cart.");
        }
        final CartModel sessionCart = cartService.getSessionCart();
        checkArgument(sessionCart.getPaymentInfo() != null, "Payment info cannot be null.");
    }
}
