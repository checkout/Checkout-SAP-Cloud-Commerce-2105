package com.checkout.hybris.events.order.process.services;

import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;

import java.util.List;

/**
 * Handle checkout.com business processes operations adding custom functionality to the {@link BusinessProcessService}
 */
public interface CheckoutComBusinessProcessService extends BusinessProcessService {

    /**
     * Finds the business processes related to the event based on the PaymentTransactionType
     *
     * @param paymentTransactionType the related payment transaction type
     * @param orderModel             the order to update
     * @param event                  the payment event
     * @return the list of found business processes
     */
    List<BusinessProcessModel> findBusinessProcess(PaymentTransactionType paymentTransactionType, OrderModel orderModel, CheckoutComPaymentEventModel event);
}