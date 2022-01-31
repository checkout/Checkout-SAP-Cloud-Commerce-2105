package com.checkout.hybris.events.daos.impl;

import com.checkout.hybris.events.daos.CheckoutComPaymentEventDao;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus.FAILED;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus.PENDING;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_APPROVED;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_CAPTURED;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@IntegrationTest
public class DefaultCheckoutComPaymentEventDaoIntegrationTest extends ServicelayerTransactionalTest {

    private static final String PAYMENT_ID_1 = "payment-id1";
    private static final String PAYMENT_ID_2 = "payment-id2";
    private static final String PAYMENT_ID_4 = "payment-id4";
    private static final String PAYMENT_ID_5 = "payment-id5";

    @Resource
    private CheckoutComPaymentEventDao checkoutComPaymentEventDao;

    @Resource
    private ModelService modelService;

    private CheckoutComPaymentEventModel pendingEvent, failedEvent;

    @Before
    public void setUp() {
        final Date yesterday = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        final Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        pendingEvent = createPaymentEvent("event1", PAYMENT_APPROVED.toString(), PENDING, PAYMENT_ID_1, null);
        createPaymentEvent("event2", PAYMENT_CAPTURED.toString(), PENDING, PAYMENT_ID_2, null);
        failedEvent = createPaymentEvent("event4", PAYMENT_APPROVED.toString(), FAILED, PAYMENT_ID_4, yesterday);
        createPaymentEvent("event5", PAYMENT_APPROVED.toString(), FAILED, PAYMENT_ID_5, tomorrow);
    }

    private CheckoutComPaymentEventModel createPaymentEvent(final String eventId, final String eventType, final CheckoutComPaymentEventStatus eventStatus, final String paymentId, final Date creationDate) {
        final CheckoutComPaymentEventModel event = modelService.create(CheckoutComPaymentEventModel.class);
        event.setCreationtime(creationDate != null ? creationDate : new Date());
        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setStatus(eventStatus);
        event.setPaymentId(paymentId);
        event.setActionId("actionId-" + eventId);
        modelService.save(event);
        return event;
    }

    @Test
    public void findPaymentEventToProcessForTypes_ShouldGetJustTheCorrectEvent() {
        final List<CheckoutComPaymentEventModel> results = checkoutComPaymentEventDao.findPaymentEventToProcessForTypes(new HashSet<>(singletonList(PAYMENT_APPROVED)));

        assertEquals(1, results.size());
        assertSame(pendingEvent, results.get(0));
    }

    @Test
    public void findPaymentEventsByStatusCreatedBeforeDate_ShouldGetEventOlderThanSpecified() {
        final List<CheckoutComPaymentEventModel> results = checkoutComPaymentEventDao.findPaymentEventsByStatusCreatedBeforeDate(FAILED, new Date());

        assertEquals(1, results.size());
        assertSame(failedEvent, results.get(0));
    }

}
