package com.checkout.hybris.events.cronjob.performables;

import com.checkout.hybris.events.model.CheckoutComPaymentEventCleanupCronJobModel;
import com.checkout.hybris.events.services.CheckoutComPaymentEventCleanupService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus.FAILED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentEventCleanupJobTest {

    private static final int AGE_IN_DAYS = 10;

    @InjectMocks
    private CheckoutComPaymentEventCleanupJob testObj;

    @Mock
    private CheckoutComPaymentEventCleanupService checkoutComPaymentEventCleanupServiceMock;
    @Mock
    private CheckoutComPaymentEventCleanupCronJobModel checkoutComPaymentEventCleanupCronJobMock;

    @Test
    public void perform() {
        when(checkoutComPaymentEventCleanupCronJobMock.getPaymentEventStatus()).thenReturn(FAILED);
        when(checkoutComPaymentEventCleanupCronJobMock.getAgeInDaysBeforeDeletion()).thenReturn(AGE_IN_DAYS);

        final PerformResult result = testObj.perform(checkoutComPaymentEventCleanupCronJobMock);

        assertEquals(CronJobResult.SUCCESS, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());

        verify(checkoutComPaymentEventCleanupServiceMock).doCleanUp(FAILED, AGE_IN_DAYS);
    }
}