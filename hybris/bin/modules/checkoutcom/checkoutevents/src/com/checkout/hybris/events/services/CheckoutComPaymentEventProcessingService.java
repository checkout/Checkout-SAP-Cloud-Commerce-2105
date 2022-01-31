package com.checkout.hybris.events.services;

import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;

import java.util.List;

/**
 * Service to handle payment event processing
 */
public interface CheckoutComPaymentEventProcessingService {

    /**
     * Processes payment events
     *
     * @param paymentEvents   the payment events to process
     * @param transactionType type of the payment event
     */
    void processPaymentEvents(List<CheckoutComPaymentEventModel> paymentEvents, PaymentTransactionType transactionType);
}
