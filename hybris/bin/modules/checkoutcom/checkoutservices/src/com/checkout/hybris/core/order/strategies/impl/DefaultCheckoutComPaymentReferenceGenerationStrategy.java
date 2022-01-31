package com.checkout.hybris.core.order.strategies.impl;

import com.checkout.hybris.core.order.strategies.CheckoutComPaymentReferenceGenerationStrategy;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.servicelayer.time.TimeService;

/**
 * Default implementation of the {@link CheckoutComPaymentReferenceGenerationStrategy}
 */
public class DefaultCheckoutComPaymentReferenceGenerationStrategy implements CheckoutComPaymentReferenceGenerationStrategy {

    protected final TimeService timeService;

    public DefaultCheckoutComPaymentReferenceGenerationStrategy(final TimeService timeService) {
        this.timeService = timeService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generatePaymentReference(final AbstractOrderModel abstractOrder) {
        return abstractOrder.getCode().concat("-").concat(String.valueOf(timeService.getCurrentTime().getTime()));
    }
}
