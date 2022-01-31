package com.checkout.hybris.fulfilmentprocess.actions.returns;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.InvoicePaymentInfoModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.task.RetryLaterException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCaptureRefundActionTest {

    private static final String OK = "OK";
    private static final String NOK = "NOK";
    private static final String WAIT = "WAIT";
    private static final String OUTCOME = "outcome";
    private static final String ACTION_ID = "actionId";
    private static final BigDecimal REFUND_AMOUNT = BigDecimal.valueOf(15.0d);

    @Spy
    @InjectMocks
    private CheckoutComCaptureRefundAction testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private ReturnProcessModel processMock;
    @Mock
    private ReturnRequestModel returnRequestMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private CheckoutComPaymentService paymentServiceMock;
    @Mock
    private PaymentTransactionModel paymentTransactionMock;
    @Mock
    private InvoicePaymentInfoModel nonCreditCardPaymentInfoMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private PaymentTransactionEntryModel refundEntryMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() {
        testObj.setModelService(modelServiceMock);

        doReturn(REFUND_AMOUNT).when(testObj).getRefundAmount(returnRequestMock);
        when(processMock.getReturnRequest()).thenReturn(returnRequestMock);
        when(returnRequestMock.getOrder()).thenReturn(orderMock);
        when(returnRequestMock.getCode()).thenReturn("returnRequestCode");
        when(orderMock.getCode()).thenReturn("orderCode");
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionMock));
        when(checkoutComPaymentTransactionServiceMock.getPaymentTransaction(orderMock)).thenReturn(paymentTransactionMock);
        when(paymentTransactionMock.getInfo()).thenReturn(checkoutComCreditCardPaymentInfoMock);
        when(checkoutComPaymentTransactionServiceMock.findRefundEntryForActionId(paymentTransactionMock, ACTION_ID)).thenReturn(Optional.of(paymentTransactionEntryMock));
    }

    @Test
    public void getTransitions_ShouldReturnTheTransitions() {
        final Set<String> transitions = testObj.getTransitions();

        assertTrue(transitions.contains(OK));
        assertTrue(transitions.contains(NOK));
        assertTrue(transitions.contains(WAIT));
    }

    @Test
    public void execute_WhenNoPaymentTransactions_ShouldReturnNOK() {
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.emptyList());

        final String result = testObj.execute(processMock);

        assertEquals(NOK, result);
    }

    @Test
    public void execute_WhenNoCreditCardPaymentInfo_ShouldReturnOK() {
        when(paymentTransactionMock.getInfo()).thenReturn(nonCreditCardPaymentInfoMock);

        final String result = testObj.execute(processMock);

        assertEquals(OK, result);
    }

    @Test
    public void execute_WhenRefundActionNotFoundInProcess_ShouldPerformCaptureAndReturnOutcome() {
        when(processMock.getRefundActionId()).thenReturn(null);
        doReturn(paymentTransactionEntryMock).when(testObj).refundPayment(any(PaymentTransactionModel.class), any(BigDecimal.class));
        doReturn(OUTCOME).when(testObj).evaluateProcessOutcome(any(ReturnRequestModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionEntryModel.class));

        final String result = testObj.execute(processMock);

        assertEquals(OUTCOME, result);
        verify(testObj).refundPayment(paymentTransactionMock, REFUND_AMOUNT);
        verify(testObj).evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);
    }

    @Test
    public void execute_WhenRefundActionNotFoundInProcessAndApmPayment_ShouldPerformCaptureAndReturnOutcome() {
        when(paymentTransactionMock.getInfo()).thenReturn(apmPaymentInfoMock);
        when(processMock.getRefundActionId()).thenReturn(null);
        doReturn(paymentTransactionEntryMock).when(testObj).refundPayment(any(PaymentTransactionModel.class), any(BigDecimal.class));
        doReturn(OUTCOME).when(testObj).evaluateProcessOutcome(any(ReturnRequestModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionEntryModel.class));

        final String result = testObj.execute(processMock);

        assertEquals(OUTCOME, result);
        verify(testObj).refundPayment(paymentTransactionMock, REFUND_AMOUNT);
        verify(testObj).evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);
    }

    @Test
    public void execute_WhenRefundActionFoundInProcess_ShouldNotPerformCaptureAndReturnOutcome() {
        when(processMock.getRefundActionId()).thenReturn(ACTION_ID);
        when(checkoutComPaymentTransactionServiceMock.findRefundEntryForActionId(paymentTransactionMock, ACTION_ID)).thenReturn(Optional.of(paymentTransactionEntryMock));
        doReturn(OUTCOME).when(testObj).evaluateProcessOutcome(any(ReturnRequestModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionEntryModel.class));

        final String result = testObj.execute(processMock);

        assertEquals(OUTCOME, result);
        verify(testObj, never()).refundPayment(paymentTransactionMock, REFUND_AMOUNT);
        verify(testObj).evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);
    }

    @Test
    public void refundPayment_WhenRefundSuccessful_ShouldReturnRefundEntry() {
        when(paymentServiceMock.refundFollowOn(paymentTransactionMock, REFUND_AMOUNT)).thenReturn(refundEntryMock);
        when(refundEntryMock.getRequestToken()).thenReturn(ACTION_ID);

        final PaymentTransactionEntryModel result = testObj.refundPayment(paymentTransactionMock, REFUND_AMOUNT);

        assertEquals(refundEntryMock, result);
    }

    @Test(expected = RetryLaterException.class)
    public void refundPayment_WhenPaymentIntegrationException_ShouldThrowRetryLaterException() {
        when(paymentServiceMock.refundFollowOn(paymentTransactionMock, REFUND_AMOUNT)).thenThrow(new CheckoutComPaymentIntegrationException("error"));

        testObj.refundPayment(paymentTransactionMock, REFUND_AMOUNT);
    }

    @Test
    public void evaluateProcessOutcome_WhenRefundEntryInUnsupportedState_ShouldSetTheReturnStatusToPaymentReversalFailedReturnNOK() {
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.REVIEW.toString());

        final String result = testObj.evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);

        assertEquals(NOK, result);
        verify(testObj).setReturnRequestStatus(returnRequestMock, ReturnStatus.PAYMENT_REVERSAL_FAILED);
    }

    @Test
    public void evaluateProcessOutcome_WhenRefundEntryPending_ShouldSetTheReturnStatusToPendingAndReturnWAIT() {
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.PENDING.toString());

        final String result = testObj.evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);

        assertEquals(WAIT, result);
        verify(testObj).setReturnRequestStatus(returnRequestMock, ReturnStatus.PAYMENT_REVERSAL_PENDING);
    }

    @Test
    public void evaluateProcessOutcome_WhenRefundEntryAccepted_ShouldSetTheReturnStatusToPaymentReversedReturnOK() {
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.ACCEPTED.toString());

        final String result = testObj.evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);

        assertEquals(OK, result);
        verify(testObj).setReturnRequestStatus(returnRequestMock, ReturnStatus.PAYMENT_REVERSED);
    }

    @Test
    public void evaluateProcessOutcome_WhenRefundEntryError_ShouldSetTheReturnStatusToPaymentReversalFailedReturnNOK() {
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.ERROR.toString());

        final String result = testObj.evaluateProcessOutcome(returnRequestMock, paymentTransactionMock, paymentTransactionEntryMock);

        assertEquals(NOK, result);
        verify(testObj).setReturnRequestStatus(returnRequestMock, ReturnStatus.PAYMENT_REVERSAL_FAILED);
    }

}