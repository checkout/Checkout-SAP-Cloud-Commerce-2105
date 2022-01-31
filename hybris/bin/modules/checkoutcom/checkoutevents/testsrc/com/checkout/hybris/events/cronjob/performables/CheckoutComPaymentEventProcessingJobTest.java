package com.checkout.hybris.events.cronjob.performables;

import com.checkout.hybris.events.daos.CheckoutComPaymentEventDao;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.model.CheckoutComPaymentEventProcessingCronJobModel;
import com.checkout.hybris.events.services.CheckoutComPaymentEventProcessingService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentEventProcessingJobTest {

    private static final HashSet<CheckoutComPaymentEventType> EVENT_TYPES = new HashSet<>(Arrays.asList(CheckoutComPaymentEventType.PAYMENT_CAPTURED, CheckoutComPaymentEventType.PAYMENT_CAPTURE_DECLINED));

    @InjectMocks
    private CheckoutComPaymentEventProcessingJob testObj;

    @Mock
    private CheckoutComPaymentEventDao checkoutComPaymentEventDaoMock;
    @Mock
    private CheckoutComPaymentEventProcessingService checkoutComPaymentEventProcessingServiceMock;
    @Mock
    private CheckoutComPaymentEventProcessingCronJobModel checkoutComPaymentEventCronJobMock;
    @Mock
    private CheckoutComPaymentEventModel checkoutComPaymentEventMock;

    @Test
    public void perform_WhenJobHasNoEventTypes_ShouldAbort() {
        when(checkoutComPaymentEventCronJobMock.getCheckoutComPaymentEventTypes()).thenReturn(Collections.emptySet());

        final PerformResult result = testObj.perform(checkoutComPaymentEventCronJobMock);

        assertEquals(CronJobResult.ERROR, result.getResult());
        assertEquals(CronJobStatus.ABORTED, result.getStatus());
    }

    @Test
    public void perform_WhenEventTypesDefined_ShouldFindAndProcessEventsForTheType() {
        when(checkoutComPaymentEventCronJobMock.getCheckoutComPaymentEventTypes()).thenReturn(EVENT_TYPES);
        when(checkoutComPaymentEventCronJobMock.getPaymentTransactionType()).thenReturn(PaymentTransactionType.CAPTURE);
        when(checkoutComPaymentEventDaoMock.findPaymentEventToProcessForTypes(EVENT_TYPES)).thenReturn(Collections.singletonList(checkoutComPaymentEventMock));

        final PerformResult result = testObj.perform(checkoutComPaymentEventCronJobMock);

        assertEquals(CronJobResult.SUCCESS, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());
        verify(checkoutComPaymentEventProcessingServiceMock).processPaymentEvents(Collections.singletonList(checkoutComPaymentEventMock), PaymentTransactionType.CAPTURE);
    }
}
