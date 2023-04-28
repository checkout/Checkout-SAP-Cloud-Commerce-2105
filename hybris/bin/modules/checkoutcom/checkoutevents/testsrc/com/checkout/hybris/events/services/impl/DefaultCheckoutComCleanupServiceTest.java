package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import com.checkout.hybris.events.daos.CheckoutComCleanupDao;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCleanupServiceTest {

    private static final int BATCH_SIZE = 2;
    private static final int AGE_IN_MONTHS = 24;
    private static final String ITEM_TYPE_CODE = "ACHConsent";

    @InjectMocks
    private DefaultCheckoutComCleanupService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CheckoutComCleanupDao checkoutComCleanupDaoMock;

    @Mock
    private ItemModel item1Mock, item2Mock;
    @Mock
    private CheckoutComCleanupCronJobModel checkoutComCleanupCronJobMock;

    @Captor
    private ArgumentCaptor<List<ItemModel>> itemModelArgumentCaptor;

    @Test
    public void doCleanUp_WhenItemTypeCodeAndAgeGiven_ShouldFindItemsAndRemoveThem() {
        when(checkoutComCleanupCronJobMock.getItemTypeCode()).thenReturn(ITEM_TYPE_CODE);
        when(checkoutComCleanupCronJobMock.getBatchSize()).thenReturn(BATCH_SIZE);
        when(checkoutComCleanupCronJobMock.getItemRemovalAge()).thenReturn(AGE_IN_MONTHS);
        when(checkoutComCleanupDaoMock.findItemsToCleanup(checkoutComCleanupCronJobMock)).thenReturn(new ArrayList<>(Arrays.asList(item1Mock, item2Mock)));

        testObj.doCleanUp(checkoutComCleanupCronJobMock);

        verify(modelServiceMock).removeAll(itemModelArgumentCaptor.capture());

        final List<ItemModel> removedItems = itemModelArgumentCaptor.getValue();
        assertThat(removedItems).containsExactlyInAnyOrder(item1Mock, item2Mock);
    }
}
