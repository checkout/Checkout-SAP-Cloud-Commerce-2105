package com.checkout.hybris.events.cronjob.performables;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import com.checkout.hybris.events.services.CheckoutComCleanupService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

/**
 * Cronjob in charge of cleaning up Checkout items
 */
public class CheckoutComCleanupJob extends AbstractJobPerformable<CheckoutComCleanupCronJobModel> {

    protected final CheckoutComCleanupService checkoutComCleanupService;

    public CheckoutComCleanupJob(final CheckoutComCleanupService checkoutComCleanupService) {
        this.checkoutComCleanupService = checkoutComCleanupService;
    }

    @Override
    public PerformResult perform(final CheckoutComCleanupCronJobModel checkoutComCleanUpCronJobModel) {
        checkoutComCleanupService.doCleanUp(checkoutComCleanUpCronJobModel);
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}
