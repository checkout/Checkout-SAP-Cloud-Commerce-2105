package com.checkout.hybris.test.inbound.events;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCronJobPersistenceHookTest {

    private static final String CRONJOB_CODE = "cronjobCode";

    @InjectMocks
    private CheckoutComCronJobPersistenceHook testObj;

    @Mock
    private CronJobService cronJobServiceMock;

    @Mock
    private ItemModel itemModelMock;
    @Mock
    private CronJobModel cronJobModelMock;
    @Mock
    private CronJobModel foundCronjobModelMock;

    @Test
    public void shouldExecuteCronjobByGivenCronJobName() {
        when(cronJobModelMock.getCode()).thenReturn(CRONJOB_CODE);
        when(cronJobServiceMock.getCronJob(CRONJOB_CODE)).thenReturn(foundCronjobModelMock);

        final Optional<ItemModel> result = testObj.execute(cronJobModelMock);

        verify(cronJobServiceMock).performCronJob(foundCronjobModelMock);
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldNotPerformACronJobWhenItemModelIsNotOfCronJobModel() {
        final Optional<ItemModel> result = testObj.execute(itemModelMock);

        verifyZeroInteractions(cronJobServiceMock);
        assertThat(result).isEmpty();
    }
}
