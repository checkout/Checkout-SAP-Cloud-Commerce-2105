package com.checkout.hybris.core.ordercancel.denialstrategies.impl;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
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
 * Implementation of a OrderCancelDenialStrategy considering the conditions of Checkout.com to do not allow a
 * order cancel if the transaction entry authorize does not exist or if the capture exists
 */
public class CheckoutComPaymentStatusOrderCancelDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComPaymentStatusOrderCancelDenialStrategy.class);

    protected final CheckoutComPaymentService paymentService;

    public CheckoutComPaymentStatusOrderCancelDenialStrategy(final CheckoutComPaymentService paymentService) {
        this.paymentService = paymentService;
    }

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

        if (paymentService.isAuthorizationPending(orderModel) || !paymentService.isCapturePending(orderModel) ||
                paymentService.isAutoCapture(orderModel) || paymentService.isDeferred(orderModel)) {
            LOG.debug("Order with code [{}] cannot be cancelled because authorization is missing or capture already executed or auto capture order", orderModel.getCode());
            return getReason();
        }

        return null;
    }
}