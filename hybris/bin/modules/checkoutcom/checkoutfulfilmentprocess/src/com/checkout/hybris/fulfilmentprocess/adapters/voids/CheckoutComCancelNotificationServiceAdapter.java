package com.checkout.hybris.fulfilmentprocess.adapters.voids;

import de.hybris.platform.ordercancel.OrderCancelNotificationServiceAdapter;
import de.hybris.platform.ordercancel.events.CancelFinishedEvent;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;
import de.hybris.platform.servicelayer.event.EventService;

/**
 * Sends cancel notifications
 */
public class CheckoutComCancelNotificationServiceAdapter implements OrderCancelNotificationServiceAdapter {

    protected final EventService eventService;

    public CheckoutComCancelNotificationServiceAdapter(final EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendCancelFinishedNotifications(final OrderCancelRecordEntryModel orderCancelRecordEntryModel) {
        eventService.publishEvent(new CancelFinishedEvent(orderCancelRecordEntryModel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendCancelPendingNotifications(final OrderCancelRecordEntryModel orderCancelRecordEntryModel) {
        //Deliberately does nothing...
    }
}
