package com.checkout.hybris.events.cronjob.performables;

import com.checkout.hybris.events.daos.CheckoutComPaymentEventDao;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.model.CheckoutComPaymentEventProcessingCronJobModel;
import com.checkout.hybris.events.services.CheckoutComPaymentEventProcessingService;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * Processing of the payment events received from webhooks
 */
public class CheckoutComPaymentEventProcessingJob extends AbstractJobPerformable<CheckoutComPaymentEventProcessingCronJobModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComPaymentEventProcessingJob.class);

    protected final CheckoutComPaymentEventDao checkoutComPaymentEventDao;
    protected final CheckoutComPaymentEventProcessingService checkoutComPaymentEventProcessingService;

    public CheckoutComPaymentEventProcessingJob(final CheckoutComPaymentEventDao checkoutComPaymentEventDao,
                                                final CheckoutComPaymentEventProcessingService checkoutComPaymentEventProcessingService) {
        this.checkoutComPaymentEventDao = checkoutComPaymentEventDao;
        this.checkoutComPaymentEventProcessingService = checkoutComPaymentEventProcessingService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PerformResult perform(final CheckoutComPaymentEventProcessingCronJobModel paymentEventProcessingCronJob) {
        final Set<CheckoutComPaymentEventType> checkoutComPaymentEventTypes = paymentEventProcessingCronJob.getCheckoutComPaymentEventTypes();
        if (CollectionUtils.isEmpty(checkoutComPaymentEventTypes)) {
            LOG.error("The cron job does not have any event type set to process.");
            return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
        }

        final List<CheckoutComPaymentEventModel> paymentEvents = checkoutComPaymentEventDao.findPaymentEventToProcessForTypes(checkoutComPaymentEventTypes);
        LOG.info("Found [{}] [{}] payment event to process", paymentEvents.size(), paymentEventProcessingCronJob.getPaymentTransactionType().toString());

        checkoutComPaymentEventProcessingService.processPaymentEvents(paymentEvents, paymentEventProcessingCronJob.getPaymentTransactionType());

        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }
}