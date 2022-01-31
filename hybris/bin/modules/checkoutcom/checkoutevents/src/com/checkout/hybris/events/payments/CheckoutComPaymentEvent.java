package com.checkout.hybris.events.payments;

import de.hybris.platform.servicelayer.event.ClusterAwareEvent;
import de.hybris.platform.servicelayer.event.PublishEventContext;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import java.util.Map;

/**
 * Event containing the body of the Webhook receieved from Checkout.com
 */
public class CheckoutComPaymentEvent extends AbstractEvent implements ClusterAwareEvent {

    private Map eventBody;

    public CheckoutComPaymentEvent(final Map eventBody) {
        this.eventBody = eventBody;
    }

    @Override
    public boolean canPublish(final PublishEventContext publishEventContext) {
        return true;
    }

    @Override
    public boolean publish(final int sourceNodeId, final int targetNodeId) {
        return (sourceNodeId == targetNodeId);
    }

    public Map getEventBody() {
        return eventBody;
    }

    public void setEventBody(final Map eventBody) {
        this.eventBody = eventBody;
    }

    @Override
    public String toString() {
        return "CheckoutComPaymentEvent{" +
                "eventBody=" + eventBody +
                '}';
    }
}