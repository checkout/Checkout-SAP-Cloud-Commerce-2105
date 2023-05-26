/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.fulfilmentprocess.actions.order;

import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.orderprocessing.events.OrderCompletedEvent;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.servicelayer.event.EventService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Send event representing the completion of an order process.
 */
public class SendOrderCompletedNotificationAction extends AbstractProceduralAction<OrderProcessModel> {
    private static final Logger LOG = LogManager.getLogger(SendOrderCompletedNotificationAction.class);

    private EventService eventService;

    @Override
    public void executeAction(final OrderProcessModel process) {
        if (OrderStatus.PAYMENT_RETURNED.equals(process.getOrder().getStatus())) {
            LOG.warn("Payment was returned for order: {}, avoiding completing order", process.getOrder().getCode());
            return;
        }
        getEventService().publishEvent(new OrderCompletedEvent(process));
        LOG.info("Process: {}  in step {}", process.getCode(), getClass());
    }

    protected EventService getEventService() {
        return eventService;
    }

    @Required
    public void setEventService(final EventService eventService) {
        this.eventService = eventService;
    }
}
