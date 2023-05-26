package com.checkout.hybris.events.daos;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import de.hybris.platform.core.model.ItemModel;

import java.util.List;

/**
 * Dao interface to get Items from DB to cleanup
 */
public interface CheckoutComCleanupDao {

    /**
     * Looks for checkout items to be cleaned up from the database
     * Only items whose creation date is older than X months will be removed
     * The amount of months used for this search is provided in the cleanup cronjob object
     *
     * @param cronJob Cleanup cronjob. The field {@code monthsOld} states how old an item must be to be removed from the DB
     * @return Items to be removed
     */
    List<ItemModel> findItemsToCleanup(CheckoutComCleanupCronJobModel cronJob);
}
