package com.checkout.hybris.fulfilmentprocess.actions.order;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Action to check authorization state on order payment
 */
public class CheckoutComCheckAuthorizeOrderPaymentAction extends AbstractAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComCheckAuthorizeOrderPaymentAction.class);

    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    protected final CheckoutComPaymentService paymentService;

    public CheckoutComCheckAuthorizeOrderPaymentAction(final CheckoutComPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(OK, NOK, WAIT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final OrderProcessModel process) {
        final OrderModel order = process.getOrder();

        if (order != null) {
            if (order.getPaymentInfo() instanceof InvoicePaymentInfoModel) {
                return OK;
            } else {
                if (paymentService.isAuthorizationPending(order)) {
                    setOrderStatus(order, OrderStatus.AUTHORIZATION_PENDING);
                    return WAIT;
                }

                if (paymentService.isAuthorizationApproved(order)) {
                    setOrderStatus(order, OrderStatus.PAYMENT_AUTHORIZED);
                    return OK;
                }

                LOG.error("Could not find a pending or approved authorisation on order [{}].", process.getCode());
                return NOK;
            }
        } else {
            LOG.error("order not found for order process with code [{}]: ", process.getCode());
            return NOK;
        }

    }
}
