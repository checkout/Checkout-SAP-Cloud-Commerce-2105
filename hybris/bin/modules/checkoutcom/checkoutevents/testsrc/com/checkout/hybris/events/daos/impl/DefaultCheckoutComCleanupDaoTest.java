package com.checkout.hybris.events.daos.impl;

import com.checkout.hybris.core.model.CheckoutComCleanupCronJobModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCleanupDaoTest {

    private static final int AGE_IN_SECONDS = 864000;
    private static final String DATE = "date";
    private static final String ITEM_TYPE_CODE = "Product";
    private static final String QUERY = "SELECT {" + ItemModel.PK + "}" +
            " FROM {" + ITEM_TYPE_CODE + "}" +
            " WHERE {" + ItemModel.CREATIONTIME + "} < ?" + DATE;

    @Spy
    @InjectMocks
    private DefaultCheckoutComCleanupDao testObj;

    @Mock
    private FlexibleSearchService flexibleSearchServiceMock;

    @Mock
    private ItemModel itemModelMock;
    @Mock
    private SearchResult<Object> searchResultMock;
    @Mock
    private CheckoutComCleanupCronJobModel cleanupCronJobModelMock;

    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> flexibleSearchQueryArgumentCaptor;

    @Test
    public void findItemsToCleanup_shouldPerformQuery_andReturnItemsToCleanUp() {
        final DateTime dateTime = new DateTime();
        when(flexibleSearchServiceMock.search(any(FlexibleSearchQuery.class))).thenReturn(searchResultMock);
        when(searchResultMock.getResult()).thenReturn(List.of(itemModelMock));
        when(cleanupCronJobModelMock.getItemRemovalAge()).thenReturn(AGE_IN_SECONDS);
        when(cleanupCronJobModelMock.getItemTypeCode()).thenReturn(ITEM_TYPE_CODE);
        doReturn(dateTime).when(testObj).getDateTime();

        final List<ItemModel> result = testObj.findItemsToCleanup(cleanupCronJobModelMock);

        assertThat(result).containsExactly(itemModelMock);
        verify(flexibleSearchServiceMock).search(flexibleSearchQueryArgumentCaptor.capture());
        final FlexibleSearchQuery query = flexibleSearchQueryArgumentCaptor.getValue();
        assertThat(query.getQuery()).isEqualTo(QUERY);
        assertThat(query.getQueryParameters()).containsExactly(Map.entry(DATE, dateTime.minusSeconds(AGE_IN_SECONDS).toDate()));
    }
}
