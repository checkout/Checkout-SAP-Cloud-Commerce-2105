package com.checkout.hybris.events.cronjob.performables;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import com.checkout.hybris.events.services.CheckoutComCleanupService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCleanupJobTest {

    private static final int AGE_IN_SECONDS = 864000;
    private static final String ITEM_TYPE_CODE = "ACHConsent";

    @InjectMocks
    private CheckoutComCleanupJob testObj;

    @Mock
    private CheckoutComCleanupService checkoutComCleanupServiceMock;
    @Mock
    private CheckoutComCleanupCronJobModel checkoutComCleanupCronJobMock;

    @Test
    public void perform_shouldCallCleanupService_andReturnSuccess() {
        when(checkoutComCleanupCronJobMock.getItemTypeCode()).thenReturn(ITEM_TYPE_CODE);
        when(checkoutComCleanupCronJobMock.getItemRemovalAge()).thenReturn(AGE_IN_SECONDS);

        final PerformResult result = testObj.perform(checkoutComCleanupCronJobMock);

        assertEquals(CronJobResult.SUCCESS, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());

        verify(checkoutComCleanupServiceMock).doCleanUp(checkoutComCleanupCronJobMock);
    }
}
