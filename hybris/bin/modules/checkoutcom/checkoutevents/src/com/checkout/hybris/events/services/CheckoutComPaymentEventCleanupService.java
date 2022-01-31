package com.checkout.hybris.events.services;

import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;

/**
 * Provides payment event cleanup functionalities
 */
public interface CheckoutComPaymentEventCleanupService {

    /**
     * Deletes payment events for a status older than the age in days
     *
     * @param eventStatus    the status of the event to be deleted
     * @param eventAgeInDays the age in days of the event prior to be deleted
     */
    void doCleanUp(CheckoutComPaymentEventStatus eventStatus, int eventAgeInDays);
}
