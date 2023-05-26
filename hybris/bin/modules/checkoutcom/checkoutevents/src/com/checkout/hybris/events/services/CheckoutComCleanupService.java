package com.checkout.hybris.events.services;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;

/**
 * Provides cleanup functionalities
 */
public interface CheckoutComCleanupService {

    /**
     * Deletes Items that are older than the age (given in months)
     *
     * @param cronJob the cronjob
     */
    void doCleanUp(CheckoutComCleanupCronJobModel cronJob);
}
