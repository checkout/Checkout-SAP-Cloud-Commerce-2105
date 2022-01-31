package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.events.daos.CheckoutComPaymentEventDao;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.services.CheckoutComPaymentEventCleanupService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link CheckoutComPaymentEventCleanupService}
 */
public class DefaultCheckoutComPaymentEventCleanupService implements CheckoutComPaymentEventCleanupService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentEventCleanupService.class);

    protected final ModelService modelService;
    protected final CheckoutComPaymentEventDao checkoutComPaymentEventDao;

    public DefaultCheckoutComPaymentEventCleanupService(final ModelService modelService, final CheckoutComPaymentEventDao checkoutComPaymentEventDao) {
        this.modelService = modelService;
        this.checkoutComPaymentEventDao = checkoutComPaymentEventDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCleanUp(final CheckoutComPaymentEventStatus eventStatus, final int eventAgeInDays) {
        final Date creationDate = Date.from(LocalDate.now().minusDays(eventAgeInDays).atStartOfDay(ZoneId.systemDefault()).toInstant());
        final List<CheckoutComPaymentEventModel> paymentEventsByStatusCreatedBeforeDate = checkoutComPaymentEventDao.findPaymentEventsByStatusCreatedBeforeDate(eventStatus, creationDate);
        paymentEventsByStatusCreatedBeforeDate.forEach(paymentEvent -> {
            LOG.info("Deleting payment event id [{}]  with status [{}]", paymentEvent.getEventId(), paymentEvent.getStatus());
            modelService.remove(paymentEvent);
        });
    }

}