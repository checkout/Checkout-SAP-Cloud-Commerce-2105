package com.checkout.hybris.backoffice.actions.order.cancel;

import com.hybris.cockpitng.actions.ActionContext;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.omsbackoffice.actions.order.cancel.CancelOrderAction;
import org.apache.commons.collections.CollectionUtils;

/**
 * Checkout.com extension of the customersupportbackoffice CancelOrderAction
 * <p>
 * Not allowing partial order or order entry cancellations as not supported
 * by Checkout.com
 */
public class CheckoutComCancelOrderAction extends CancelOrderAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canPerform(final ActionContext<OrderModel> actionContext) {
        OrderModel order = (OrderModel) actionContext.getData();
        return order != null && CollectionUtils.isNotEmpty(order.getEntries()) &&
                getOrderCancelService().isCancelPossible(order, getUserService().getCurrentUser(), false, false).isAllowed() &&
                !getNotCancellableOrderStatus().contains(order.getStatus());
    }
}