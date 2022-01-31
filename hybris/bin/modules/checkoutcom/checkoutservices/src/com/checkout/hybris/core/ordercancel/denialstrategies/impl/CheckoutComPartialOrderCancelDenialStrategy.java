package com.checkout.hybris.core.ordercancel.denialstrategies.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.impl.denialstrategies.AbstractCancelDenialStrategy;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Implementation of a OrderCancelDenialStrategy considering the conditions of Checkout.com to do not allow a partial
 * order cancel
 */
public class CheckoutComPartialOrderCancelDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComPartialOrderCancelDenialStrategy.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel orderCancelConfigModel,
                                                         final OrderModel orderModel,
                                                         final PrincipalModel principalModel,
                                                         final boolean partialCancel,
                                                         final boolean partialEntryCancel) {
        validateParameterNotNull(orderCancelConfigModel, "Order cancel config cannot be null");
        validateParameterNotNull(orderModel, "OrderModel cannot be null");

        if (partialCancel || partialEntryCancel) {
            LOG.debug("The partial cancel is not allowed.");
            return getReason();
        }

        return null;
    }
}