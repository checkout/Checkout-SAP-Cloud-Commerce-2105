package com.checkout.hybris.events.daos.impl;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import com.checkout.hybris.events.daos.CheckoutComCleanupDao;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link CheckoutComCleanupDao}
 */
public class DefaultCheckoutComCleanupDao implements CheckoutComCleanupDao {

    private static final String DATE = "date";

    private final FlexibleSearchService flexibleSearchService;

    public DefaultCheckoutComCleanupDao(final FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemModel> findItemsToCleanup(final CheckoutComCleanupCronJobModel cronJob) {
        final Date date = getDateTime().minusSeconds(cronJob.getItemRemovalAge()).toDate();
        String strQuery = "SELECT {" + ItemModel.PK + "}" +
                " FROM {" + cronJob.getItemTypeCode() + "}" +
                " WHERE {" + ItemModel.CREATIONTIME + "} < ?" + DATE;

        final FlexibleSearchQuery query = new FlexibleSearchQuery(strQuery);
        query.addQueryParameter(DATE, date);
        final SearchResult<ItemModel> result = flexibleSearchService.search(query);

        return result.getResult();
    }

    protected DateTime getDateTime() {
        return new DateTime();
    }

}
