package com.checkout.hybris.core.payment.details.strategies.impl;

import com.checkout.hybris.core.payment.details.mappers.CheckoutComUpdatePaymentInfoStrategyMapper;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import de.hybris.platform.order.CartService;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MADA;

/**
 * Implements the Mada payment logic to update the payment info on payment success response
 */
public class CheckoutComMadaUpdatePaymentInfoStrategy extends CheckoutComCardUpdatePaymentInfoStrategy {

    public CheckoutComMadaUpdatePaymentInfoStrategy(final CartService cartService,
                                                    final CheckoutComPaymentInfoService paymentInfoService,
                                                    final CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapper) {
        super(cartService, paymentInfoService, checkoutComUpdatePaymentInfoStrategyMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return MADA;
    }
}

