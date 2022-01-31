package com.checkout.hybris.events.order.process.services.impl;

import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.order.process.daos.CheckoutComProcessDefinitionDao;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static de.hybris.platform.payment.enums.PaymentTransactionType.*;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComBusinessProcessServiceTest {

    private static final String ACTION_ID = "actionId";
    private static final String ORDER_CODE = "order";
    private static final String BUSINESS_PROCESS_CODE = "businessProcessCode";
    private static final String ORDER_PROCESS_NAME = "order-process";
    private static final String EVENT_ID = "event_id";

    @InjectMocks
    private DefaultCheckoutComBusinessProcessService testObj;

    @Mock
    private CheckoutComProcessDefinitionDao checkoutComProcessDefinitionDaoMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private BusinessProcessModel businessProcessMock;
    @Mock
    private CheckoutComPaymentEventModel eventMock;
    @Mock
    private BaseStoreModel baseStoreMock;

    @Before
    public void setUp() {
        when(orderMock.getCode()).thenReturn(ORDER_CODE);
        when(orderMock.getStore()).thenReturn(baseStoreMock);
        when(baseStoreMock.getSubmitOrderProcessCode()).thenReturn(ORDER_PROCESS_NAME);
        when(businessProcessMock.getCode()).thenReturn(BUSINESS_PROCESS_CODE);
        when(eventMock.getActionId()).thenReturn(ACTION_ID);
        when(eventMock.getEventId()).thenReturn(EVENT_ID);
    }

    @Test
    public void findBusinessProcess_WhenTransactionTypeRefund_ShouldFindReturnProcess() {
        when(checkoutComProcessDefinitionDaoMock.findWaitingReturnProcesses(ACTION_ID)).thenReturn(singletonList(businessProcessMock));

        final List<BusinessProcessModel> result = testObj.findBusinessProcess(REFUND_FOLLOW_ON, orderMock, eventMock);

        assertSame(businessProcessMock, result.get(0));
        assertEquals(BUSINESS_PROCESS_CODE, result.get(0).getCode());
    }

    @Test
    public void findBusinessProcess_WhenTransactionTypeCancel_ShouldFindVoidProcess() {
        when(checkoutComProcessDefinitionDaoMock.findWaitingVoidProcesses(ORDER_CODE)).thenReturn(singletonList(businessProcessMock));

        final List<BusinessProcessModel> result = testObj.findBusinessProcess(CANCEL, orderMock, eventMock);

        assertSame(businessProcessMock, result.get(0));
        assertEquals(BUSINESS_PROCESS_CODE, result.get(0).getCode());
    }

    @Test
    public void findBusinessProcess_WhenTransactionTypeAuthorization_ShouldFindTheOrderProcess() {
        when(checkoutComProcessDefinitionDaoMock.findWaitingOrderProcesses(ORDER_CODE, ORDER_PROCESS_NAME)).thenReturn(singletonList(businessProcessMock));

        final List<BusinessProcessModel> result = testObj.findBusinessProcess(AUTHORIZATION, orderMock, eventMock);

        assertSame(businessProcessMock, result.get(0));
        assertEquals(BUSINESS_PROCESS_CODE, result.get(0).getCode());
    }
}