package com.checkout.hybris.events.facades;

/**
 * Facade to orchestrate logic for checkout.com events
 */
public interface CheckoutComEventFacade {

    /**
     * Publishes the payment event
     *
     * @param eventBody the event body
     */
    void publishPaymentEvent(String eventBody);
}
