package com.checkout.hybris.core.order.strategies;

import de.hybris.platform.core.model.order.AbstractOrderModel;

/**
 * Used to generate a payment reference to be used in a payment request
 */
public interface CheckoutComPaymentReferenceGenerationStrategy {

    /**
     * Generates a payment reference
     *
     * @param abstractOrder An order
     * @return the generated payment reference
     */
    String generatePaymentReference(AbstractOrderModel abstractOrder);

}
