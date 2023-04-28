package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import com.checkout.hybris.events.daos.CheckoutComCleanupDao;
import com.checkout.hybris.events.services.CheckoutComCleanupService;
import com.google.common.collect.Lists;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Default implementation of {@link CheckoutComCleanupService}
 */
public class DefaultCheckoutComCleanupService implements CheckoutComCleanupService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComCleanupService.class);

    protected final ModelService modelService;
    protected final CheckoutComCleanupDao checkoutComCleanupDao;

    public DefaultCheckoutComCleanupService(final ModelService modelService, final CheckoutComCleanupDao checkoutComCleanupDao) {
        this.modelService = modelService;
        this.checkoutComCleanupDao = checkoutComCleanupDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCleanUp(final CheckoutComCleanupCronJobModel cronJob) {
        final List<ItemModel> rows = checkoutComCleanupDao.findItemsToCleanup(cronJob);
        final List<List<ItemModel>> batches = Lists.partition(rows, cronJob.getBatchSize());
        batches.forEach(modelService::removeAll);
    }

}
