package com.checkout.hybris.core.payment.services;

import de.hybris.platform.core.model.order.AbstractOrderModel;

/**
 * Service in charge of handling payment returns
 */
public interface CheckoutComPaymentReturnedService {


    /**
     * Processes an order whose payment was returned
     *
     * @param order Order whose payment is returned
     */
    void handlePaymentReturned(AbstractOrderModel order);
}
