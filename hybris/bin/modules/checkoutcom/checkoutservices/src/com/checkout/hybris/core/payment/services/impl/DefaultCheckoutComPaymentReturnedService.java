package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentReturnedService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Default implementation of the {@link CheckoutComPaymentReturnedService}
 */
public class DefaultCheckoutComPaymentReturnedService implements CheckoutComPaymentReturnedService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentReturnedService.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePaymentReturned(final AbstractOrderModel order) {
        LOG.info("A payment returned event has been received for order {}", order.getCode());
    }
}
