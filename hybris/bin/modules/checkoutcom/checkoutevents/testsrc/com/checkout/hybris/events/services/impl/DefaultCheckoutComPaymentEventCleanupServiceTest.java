package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.events.daos.CheckoutComPaymentEventDao;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus.FAILED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentEventCleanupServiceTest {

    private static final int EVENT_AGE_IN_DAYS = 10;

    @InjectMocks
    private DefaultCheckoutComPaymentEventCleanupService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CheckoutComPaymentEventDao checkoutComPaymentEventDaoMock;
    @Mock
    private CheckoutComPaymentEventModel event1Mock, event2Mock;
    @Captor
    private ArgumentCaptor<Date> dateArgumentCaptor;

    @Test
    public void doCleanUp_WhenEventStatusAndEventAgeGiven_ShouldFindEventsAndRemoveThem() {
        when(checkoutComPaymentEventDaoMock.findPaymentEventsByStatusCreatedBeforeDate(eq(FAILED), any(Date.class))).thenReturn(Arrays.asList(event1Mock, event2Mock));

        testObj.doCleanUp(FAILED, EVENT_AGE_IN_DAYS);

        verify(modelServiceMock).remove(event1Mock);
        verify(modelServiceMock).remove(event2Mock);

        verify(checkoutComPaymentEventDaoMock).findPaymentEventsByStatusCreatedBeforeDate(eq(FAILED), dateArgumentCaptor.capture());

        final Date dateInPast = dateArgumentCaptor.getValue();

        long diffInMillies = Math.abs(new Date().getTime() - dateInPast.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        assertEquals(diff, EVENT_AGE_IN_DAYS);
    }
}