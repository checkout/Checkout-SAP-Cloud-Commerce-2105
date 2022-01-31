package com.checkout.hybris.fulfilmentprocess.actions.order;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCheckAuthorizeOrderPaymentActionTest {

    private static final String TRANSITION_OK = "OK";
    private static final String TRANSITION_NOK = "NOK";
    private static final String TRANSITION_WAIT = "WAIT";

    @InjectMocks
    private CheckoutComCheckAuthorizeOrderPaymentAction testObj;

    @Mock
    private CheckoutComPaymentService paymentServiceMock;
    @Mock
    private OrderProcessModel processMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;
    @Mock
    private InvoicePaymentInfoModel invoicePaymentInfoMock;
    @Mock
    private ModelService modelServiceMock;

    @Before
    public void setUp() {
        testObj.setModelService(modelServiceMock);

        when(processMock.getOrder()).thenReturn(orderMock);
        when(orderMock.getPaymentInfo()).thenReturn(paymentInfoMock);
        when(paymentServiceMock.isAutoCapture(orderMock)).thenReturn(false);
        when(paymentServiceMock.isAuthorizationPending(orderMock)).thenReturn(false);
        when(paymentServiceMock.isAuthorizationApproved(orderMock)).thenReturn(true);
    }

    @Test
    public void getTransitions_ShouldReturnTheTransactions() {
        final Set<String> transitions = testObj.getTransitions();

        assertTrue(transitions.contains(TRANSITION_OK));
        assertTrue(transitions.contains(TRANSITION_NOK));
        assertTrue(transitions.contains(TRANSITION_WAIT));
    }

    @Test
    public void execute_WhenOrderNull_ShouldReturnNOK() {
        when(processMock.getOrder()).thenReturn(null);

        final String result = testObj.execute(processMock);

        assertEquals(TRANSITION_NOK, result);
        verifyZeroInteractions(paymentServiceMock);
    }

    @Test
    public void execute_InvoicePayment_ShouldReturnOK() {
        when(orderMock.getPaymentInfo()).thenReturn(invoicePaymentInfoMock);

        final String result = testObj.execute(processMock);

        assertEquals(TRANSITION_OK, result);
        verifyZeroInteractions(paymentServiceMock);
    }

    @Test
    public void execute_NonInvoiceAndAuthPending_ShouldReturnWaitForAuthAndSetOrderStatusToPending() {
        when(paymentServiceMock.isAuthorizationPending(orderMock)).thenReturn(true);

        final String result = testObj.execute(processMock);

        assertEquals(TRANSITION_WAIT, result);
        verify(orderMock).setStatus(OrderStatus.AUTHORIZATION_PENDING);
    }

    @Test
    public void execute_NonInvoiceAndAuthNotApproved_ShouldReturnNok() {
        when(paymentServiceMock.isAuthorizationApproved(orderMock)).thenReturn(false);

        final String result = testObj.execute(processMock);

        assertEquals(TRANSITION_NOK, result);
    }

    @Test
    public void execute_NonInvoiceAndAuthApprovedWithNoAutoCapture_ShouldReturnOkAndSetOrderStatusToAuthorized() {
        final String result = testObj.execute(processMock);

        assertEquals(TRANSITION_OK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_AUTHORIZED);
    }
}
