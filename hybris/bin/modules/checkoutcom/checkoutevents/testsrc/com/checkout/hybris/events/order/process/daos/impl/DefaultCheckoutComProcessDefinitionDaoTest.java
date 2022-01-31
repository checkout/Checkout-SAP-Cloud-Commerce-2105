package com.checkout.hybris.events.order.process.daos.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComProcessDefinitionDaoTest {

    protected static final String ORDER_PROCESS_NAME = "orderProcessName";
    private static final String ORDER_CODE = "orderCode";
    private static final String REFUND_ACTION_ID = "refundActionId";
    private static final String QUERY_PARAM_ORDER_CODE = "orderCode";
    private static final String QUERY_PARAM_REFUND_ACTION_ID = "refundActionId";

    @InjectMocks
    private DefaultCheckoutComProcessDefinitionDao testObj;

    @Mock
    private FlexibleSearchService flexibleSearchServiceMock;
    @Mock
    private SearchResult searchResultMock;
    @Mock
    private BusinessProcessModel businessProcessModelMock;
    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> queryArgumentCaptor;

    @Before
    public void setUp() {
        final List<BusinessProcessModel> resultMock = Collections.singletonList(businessProcessModelMock);
        when(searchResultMock.getResult()).thenReturn(resultMock);
        when(flexibleSearchServiceMock.search(queryArgumentCaptor.capture())).thenReturn(searchResultMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findWaitingOrderProcesses_WhenOrderCodeNull_ShouldThrowException() {
        testObj.findWaitingOrderProcesses(null, ORDER_PROCESS_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findWaitingOrderProcesses_WhenOrderProcessCodeNull_ShouldThrowException() {
        testObj.findWaitingOrderProcesses(ORDER_CODE, null);
    }

    @Test
    public void findWaitingOrderProcesses_WhenEverythingIsCorrect_ShouldReturnTheBusinessProcess() {
        final List<BusinessProcessModel> result = testObj.findWaitingOrderProcesses(ORDER_CODE, ORDER_PROCESS_NAME);

        assertEquals(1, result.size());
        assertSame(businessProcessModelMock, result.get(0));

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery queryArgumentCaptorValue = queryArgumentCaptor.getValue();

        assertEquals(ORDER_CODE, queryArgumentCaptorValue.getQueryParameters().get(QUERY_PARAM_ORDER_CODE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findWaitingReturnProcesses_WhenRefundActionIdNull_ShouldThrowException() {
        testObj.findWaitingReturnProcesses(null);
    }

    @Test
    public void findWaitingReturnProcesses_WhenInputCorrect_ShouldReturnTheBusinessProcess() {
        final List<BusinessProcessModel> result = testObj.findWaitingReturnProcesses(REFUND_ACTION_ID);

        assertEquals(1, result.size());
        assertSame(businessProcessModelMock, result.get(0));

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery queryArgumentCaptorValue = queryArgumentCaptor.getValue();

        assertEquals(REFUND_ACTION_ID, queryArgumentCaptorValue.getQueryParameters().get(QUERY_PARAM_REFUND_ACTION_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findWaitingVoidProcesses_WhenOrderCodeNull_ShouldThrowException() {
        testObj.findWaitingVoidProcesses(null);
    }

    @Test
    public void findWaitingVoidProcesses_WhenInputCorrect_ShouldReturnTheBusinessProcess() {
        final List<BusinessProcessModel> result = testObj.findWaitingVoidProcesses(ORDER_CODE);

        assertEquals(1, result.size());
        assertSame(businessProcessModelMock, result.get(0));

        verify(flexibleSearchServiceMock).search(queryArgumentCaptor.capture());
        final FlexibleSearchQuery queryArgumentCaptorValue = queryArgumentCaptor.getValue();

        assertEquals(ORDER_CODE, queryArgumentCaptorValue.getQueryParameters().get(QUERY_PARAM_ORDER_CODE));
    }
}
