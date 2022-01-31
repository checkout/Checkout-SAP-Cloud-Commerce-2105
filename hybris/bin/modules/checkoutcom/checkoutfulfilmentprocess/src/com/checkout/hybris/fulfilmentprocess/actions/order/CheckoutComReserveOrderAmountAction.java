package com.checkout.hybris.fulfilmentprocess.actions.order;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Action which checks whether the authorised has been successfully reserved. This depends on the validation
 * of the authorisation amount in order to prevent order manipulations to occur. If the authorised amount
 * is not correct (or as expected) then the action fails and the order status is set accordingly.
 */
public class CheckoutComReserveOrderAmountAction extends AbstractSimpleDecisionAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComReserveOrderAmountAction.class);

    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComReserveOrderAmountAction(final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService) {
        this.checkoutComPaymentTransactionService = checkoutComPaymentTransactionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transition executeAction(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();
        final boolean authorisedAmountCorrect = checkoutComPaymentTransactionService.isAuthorisedAmountCorrect(order);
        if (!authorisedAmountCorrect) {
            LOG.warn("The authorisation amount does not match the order [{}] value.", order.getCode());
            callSuperOrderStatus(order, OrderStatus.PAYMENT_AMOUNT_NOT_RESERVED);
            return Transition.NOK;
        }
        callSuperOrderStatus(order, OrderStatus.PAYMENT_AMOUNT_RESERVED);
        return Transition.OK;
    }

    protected void callSuperOrderStatus(final OrderModel order, final OrderStatus status) {
        super.setOrderStatus(order, status);
    }

}
