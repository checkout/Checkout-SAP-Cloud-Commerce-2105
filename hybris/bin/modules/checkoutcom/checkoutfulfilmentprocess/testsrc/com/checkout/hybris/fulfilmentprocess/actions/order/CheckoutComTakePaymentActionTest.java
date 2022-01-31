package com.checkout.hybris.fulfilmentprocess.actions.order;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.task.RetryLaterException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComTakePaymentActionTest {

    private static final String OK = "OK";
    private static final String NOK = "NOK";
    private static final String WAIT = "WAIT";

    @Spy
    @InjectMocks
    private CheckoutComTakePaymentAction testObj;

    @Mock
    private CheckoutComPaymentService paymentServiceMock;
    @Mock
    private OrderProcessModel orderProcessMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoModelMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel redirectApmPaymentInfoMock;
    @Mock
    private InvoicePaymentInfoModel invoicePaymentInfoMock;
    @Mock
    private PaymentTransactionModel paymentTransactionMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() {
        testObj.setModelService(modelServiceMock);

        when(orderProcessMock.getOrder()).thenReturn(orderMock);
        when(orderMock.getPaymentTransactions()).thenReturn(singletonList(paymentTransactionMock));
        when(paymentTransactionMock.getInfo()).thenReturn(paymentInfoModelMock);
        when(paymentServiceMock.isAutoCapture(orderMock)).thenReturn(false);
        when(checkoutComPaymentTransactionServiceMock.getPaymentTransaction(orderMock)).thenReturn(paymentTransactionMock);
        when(paymentTransactionEntryMock.getPaymentTransaction()).thenReturn(paymentTransactionMock);
        when(paymentTransactionMock.getOrder()).thenReturn(orderMock);
        when(paymentServiceMock.capture(paymentTransactionMock)).thenReturn(paymentTransactionEntryMock);
    }

    @Test
    public void getTransitions_ShouldReturnTheTransitions() {
        final Set<String> transitions = testObj.getTransitions();

        assertTrue(transitions.contains(OK));
        assertTrue(transitions.contains(NOK));
        assertTrue(transitions.contains(WAIT));
    }

    @Test
    public void execute_WhenOrderIsNull_ShouldReturnNOK() {
        when(orderProcessMock.getOrder()).thenReturn(null);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verifyZeroInteractions(paymentServiceMock);
    }

    @Test
    public void execute_WhenThereAreNoTransactions_ShouldReturnNOK() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
        verifyZeroInteractions(paymentServiceMock);
    }

    @Test
    public void execute_WhenCaptureIsApprovedForAutoCapture_ShouldReturnOK() {
        when(paymentServiceMock.isAutoCapture(orderMock)).thenReturn(true);
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(OK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_CAPTURED);
    }

    @Test
    public void execute_WhenThereIsErrorForAutoCapture_ShouldReturnNOK() {
        when(paymentServiceMock.isAutoCapture(orderMock)).thenReturn(true);
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderMock)).thenReturn(false);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
    }

    @Test
    public void execute_WhenCaptureIsApprovedForAlreadyCapturedProcess_ShouldReturnOK() {
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(OK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_CAPTURED);
    }

    @Test
    public void execute_WhenCaptureIsPendingForAlreadyCapturedProcess_ShouldReturnWAIT() {
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderMock)).thenReturn(true);
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(WAIT, result);
        verify(orderMock).setStatus(OrderStatus.CAPTURE_PENDING);
    }

    @Test
    public void execute_WhenThereIsErrorForAlreadyCapturedProcess_ShouldReturnNOK() {
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(true);
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderMock)).thenReturn(false);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
    }

    @Test(expected = RetryLaterException.class)
    public void execute_WhenCaptureThrowsPaymentIntegrationExeption_ShouldThrowRetryLaterException() {
        when(paymentServiceMock.capture(paymentTransactionMock)).thenThrow(new CheckoutComPaymentIntegrationException("error"));

        testObj.execute(orderProcessMock);
    }

    @Test
    public void execute_WhenNoCaptureAndNoAutoCapture_ShouldCaptureAndReturnOKIfPaymentAccepted() {
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(false);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.ACCEPTED.toString());

        final String result = testObj.execute(orderProcessMock);

        assertEquals(OK, result);
        verify(paymentServiceMock).capture(paymentTransactionMock);
    }

    @Test
    public void execute_WhenNoCaptureAndNoAutoCapture_ShouldCaptureAndReturnNOKIfPaymentError() {
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(false);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.ERROR.toString());

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verify(paymentServiceMock).capture(paymentTransactionMock);
    }

    @Test
    public void execute_WhenNoCaptureAndNoAutoCapture_ShouldCaptureAndReturnNOKIfPaymentRejected() {
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(false);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.REJECTED.toString());

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verify(paymentServiceMock).capture(paymentTransactionMock);
    }

    @Test
    public void execute_WhenNoCaptureAndNoAutoCapture_ShouldCaptureAndReturnWAITOtherwise() {
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);
        when(paymentServiceMock.captureExists(orderMock)).thenReturn(false);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.PENDING.toString());

        final String result = testObj.execute(orderProcessMock);

        assertEquals(WAIT, result);
        verify(paymentServiceMock).capture(paymentTransactionMock);
    }

    @Test
    public void execute_WhenRedirectPaymentAlreadyCaptured_ShouldReturnOK() {
        when(paymentTransactionMock.getInfo()).thenReturn(redirectApmPaymentInfoMock);
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(true);
        when(paymentServiceMock.isDeferred(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(OK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_CAPTURED);
    }

    @Test
    public void execute_WhenRedirectPaymentCapturePending_ShouldReturnWAIT() {
        when(paymentTransactionMock.getInfo()).thenReturn(redirectApmPaymentInfoMock);
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderMock)).thenReturn(true);
        when(paymentServiceMock.isDeferred(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(WAIT, result);
        verify(orderMock).setStatus(OrderStatus.CAPTURE_PENDING);
    }

    @Test
    public void execute_WhenRedirectPaymentNotCaptured_ShouldReturnNOK() {
        when(paymentTransactionMock.getInfo()).thenReturn(redirectApmPaymentInfoMock);
        when(paymentServiceMock.isCaptureApproved(orderMock)).thenReturn(false);
        when(paymentServiceMock.isCapturePending(orderMock)).thenReturn(false);
        when(paymentServiceMock.isDeferred(orderMock)).thenReturn(true);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(NOK, result);
        verify(orderMock).setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
    }

    @Test
    public void execute_WhenInvoicePaymentInfo_ShouldReturnOK() {
        when(paymentTransactionMock.getInfo()).thenReturn(invoicePaymentInfoMock);

        final String result = testObj.execute(orderProcessMock);

        assertEquals(OK, result);
    }
}
