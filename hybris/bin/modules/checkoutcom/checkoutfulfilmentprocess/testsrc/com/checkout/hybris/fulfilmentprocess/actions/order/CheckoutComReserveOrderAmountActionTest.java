package com.checkout.hybris.fulfilmentprocess.actions.order;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComReserveOrderAmountActionTest {


    @Spy
    @InjectMocks
    private CheckoutComReserveOrderAmountAction testObj;

    @Mock
    private OrderProcessModel orderProcessMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() {
        doNothing().when(testObj).callSuperOrderStatus(any(OrderModel.class), any(OrderStatus.class));
    }

    @Test
    public void executeAction_WhenAuthoriseAmountCorrect_ShouldSetCorrectOrderStatusAndSucceed() {
        when(orderProcessMock.getOrder()).thenReturn(orderMock);
        when(checkoutComPaymentTransactionServiceMock.isAuthorisedAmountCorrect(orderMock)).thenReturn(true);

        final AbstractSimpleDecisionAction.Transition result = testObj.executeAction(orderProcessMock);

        assertEquals(AbstractSimpleDecisionAction.Transition.OK, result);
        verify(testObj).callSuperOrderStatus(orderMock, OrderStatus.PAYMENT_AMOUNT_RESERVED);
    }

    @Test
    public void executeAction_WhenAuthoriseAmountIncorrect_ShouldSetCorrectOrderStatusAndFail() {
        when(orderProcessMock.getOrder()).thenReturn(orderMock);
        when(checkoutComPaymentTransactionServiceMock.isAuthorisedAmountCorrect(orderMock)).thenReturn(false);

        final AbstractSimpleDecisionAction.Transition result = testObj.executeAction(orderProcessMock);

        assertEquals(AbstractSimpleDecisionAction.Transition.NOK, result);
        verify(testObj).callSuperOrderStatus(orderMock, OrderStatus.PAYMENT_AMOUNT_NOT_RESERVED);
    }

}