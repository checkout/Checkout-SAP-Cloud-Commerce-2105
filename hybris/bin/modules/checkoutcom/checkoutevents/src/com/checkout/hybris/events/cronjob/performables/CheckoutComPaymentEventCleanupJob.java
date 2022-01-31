package com.checkout.hybris.events.cronjob.performables;

import com.checkout.hybris.events.model.CheckoutComPaymentEventCleanupCronJobModel;
import com.checkout.hybris.events.services.CheckoutComPaymentEventCleanupService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

/**
 * Cleanup of the payment events received from webhooks
 */
public class CheckoutComPaymentEventCleanupJob extends AbstractJobPerformable<CheckoutComPaymentEventCleanupCronJobModel> {

    protected final CheckoutComPaymentEventCleanupService checkoutComPaymentEventCleanupService;

    public CheckoutComPaymentEventCleanupJob(final CheckoutComPaymentEventCleanupService checkoutComPaymentEventCleanupService) {
        this.checkoutComPaymentEventCleanupService = checkoutComPaymentEventCleanupService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PerformResult perform(final CheckoutComPaymentEventCleanupCronJobModel checkoutComPaymentEventCleanUpCronJobModel) {
        checkoutComPaymentEventCleanupService.doCleanUp(checkoutComPaymentEventCleanUpCronJobModel.getPaymentEventStatus(), checkoutComPaymentEventCleanUpCronJobModel.getAgeInDaysBeforeDeletion());
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}