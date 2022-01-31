package com.checkout.hybris.core.ordercancel.denialstrategies.impl;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.DefaultOrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComPaymentStatusOrderCancelDenialStrategyTest {

    @Spy
    @InjectMocks
    private CheckoutComPaymentStatusOrderCancelDenialStrategy testObj;

    @Mock
    private OrderCancelConfigModel orderCancelConfigModelMock;
    @Mock
    private OrderModel orderModelMock;
    @Mock
    private PrincipalModel principalModelMock;
    @Mock
    private CheckoutComPaymentService paymentServiceMock;

    private DefaultOrderCancelDenialReason denialReason = new DefaultOrderCancelDenialReason();

    @Before
    public void setUp() {
        when(paymentServiceMock.isCapturePending(orderModelMock)).thenReturn(false);
        when(paymentServiceMock.isAutoCapture(orderModelMock)).thenReturn(true);
        when(paymentServiceMock.isAuthorizationPending(orderModelMock)).thenReturn(true);
        when(paymentServiceMock.isDeferred(orderModelMock)).thenReturn(false);
        doReturn(denialReason).when(testObj).getReason();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCancelDenialReason_WhenOrderIsNull_ShouldThrowException() {
        testObj.getCancelDenialReason(orderCancelConfigModelMock, null, principalModelMock, false, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCancelDenialReason_WhenOrderCancelConfigIsNull_ShouldThrowException() {
        testObj.getCancelDenialReason(null, orderModelMock, principalModelMock, false, false);
    }

    @Test
    public void getCancelDenialReason_WhenIsAuthorizationPending_ShouldReturnTheDecision() {
        when(paymentServiceMock.isCapturePending(orderModelMock)).thenReturn(true);
        when(paymentServiceMock.isAutoCapture(orderModelMock)).thenReturn(false);

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelMock, principalModelMock, false, false);

        assertEquals(denialReason, result);
    }

    @Test
    public void getCancelDenialReason_WhenIsCaptureNotPending_ShouldReturnTheDecision() {
        when(paymentServiceMock.isAuthorizationPending(orderModelMock)).thenReturn(false);
        when(paymentServiceMock.isAutoCapture(orderModelMock)).thenReturn(false);

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelMock, principalModelMock, false, false);

        assertEquals(denialReason, result);
    }

    @Test
    public void getCancelDenialReason_WhenIsAutoCaptureOrder_ShouldReturnTheDecision() {
        when(paymentServiceMock.isAuthorizationPending(orderModelMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderModelMock)).thenReturn(true);

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelMock, principalModelMock, false, false);

        assertEquals(denialReason, result);
    }

    @Test
    public void getCancelDenialReason_WhenIsDeferredOrder_ShouldReturnTheDecision() {
        when(paymentServiceMock.isAuthorizationPending(orderModelMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderModelMock)).thenReturn(true);
        when(paymentServiceMock.isAutoCapture(orderModelMock)).thenReturn(false);
        when(paymentServiceMock.isDeferred(orderModelMock)).thenReturn(true);

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelMock, principalModelMock, false, false);

        assertEquals(denialReason, result);
    }

    @Test
    public void getCancelDenialReason_WhenOrderCanBeVoided_ShouldReturnNull() {
        when(paymentServiceMock.isAuthorizationPending(orderModelMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderModelMock)).thenReturn(true);
        when(paymentServiceMock.isAutoCapture(orderModelMock)).thenReturn(false);

        final OrderCancelDenialReason result = testObj.getCancelDenialReason(orderCancelConfigModelMock, orderModelMock, principalModelMock, false, false);

        assertNull(result);
    }
}
