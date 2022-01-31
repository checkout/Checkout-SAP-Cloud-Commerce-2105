package com.checkout.hybris.events.daos;

import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Dao interface to get CheckoutComPaymentEvents from DB
 */
public interface CheckoutComPaymentEventDao {

    /**
     * Finds all the event types to process for the given event types
     *
     * @param checkoutComPaymentEventTypes all event types to search
     * @return a list of CheckoutComPaymentEventModel
     */
    List<CheckoutComPaymentEventModel> findPaymentEventToProcessForTypes(Set<CheckoutComPaymentEventType> checkoutComPaymentEventTypes);

    /**
     * Finds all payment events with a processing status and older than the creation date
     *
     * @param paymentEventStatus processing status of the event
     * @param creationDate       creation date of the event
     * @return a list of CheckoutComPaymentEventModel
     */
    List<CheckoutComPaymentEventModel> findPaymentEventsByStatusCreatedBeforeDate(CheckoutComPaymentEventStatus paymentEventStatus, Date creationDate);
}
