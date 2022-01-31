package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.CheckoutApiException;
import com.checkout.common.ApiResponseInfo;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.payments.RefundRequest;
import com.checkout.payments.RefundResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.commands.result.RefundResult;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComRefundCommandTest {

    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String AMOUNT = "100";
    private static final String MERCHANT_TRANSACTION_CODE = "ORDER-REFERENCE-1-SOMETHING";
    private static final String PAYMENT_REFERENCE = "ORDER-REFERENCE";
    private static final Date DATE = new Date();
    private static final String ACTION_ID = "Action_id";
    private static final BigDecimal ORDER_TOTAL_PRICE = new BigDecimal("100");

    @InjectMocks
    private CheckoutComRefundCommand testObj;

    @Mock
    private CheckoutComRequestFactory checkoutComRequestFactoryMock;
    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private FollowOnRefundRequest refundRequestMock;
    @Mock
    private RefundRequest refundRequest;
    @Mock
    private RefundResponse refundResponseMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        final Currency currency = Currency.getInstance(Locale.UK);
        when(refundRequestMock.getCurrency()).thenReturn(currency);
        when(refundRequestMock.getRequestId()).thenReturn(PAYMENT_ID);
        when(refundRequestMock.getTotalAmount()).thenReturn(new BigDecimal(AMOUNT));
        when(refundRequestMock.getMerchantTransactionCode()).thenReturn(MERCHANT_TRANSACTION_CODE);
        when(checkoutComRequestFactoryMock.createRefundPaymentRequest(new BigDecimal(AMOUNT), PAYMENT_REFERENCE, currency.getCurrencyCode())).thenReturn(refundRequest);
        when(timeServiceMock.getCurrentTime()).thenReturn(DATE);
        when(refundRequest.getAmount()).thenReturn(10000l);
        when(refundRequest.getReference()).thenReturn(PAYMENT_REFERENCE);
        when(checkoutComPaymentIntegrationServiceMock.refundPayment(refundRequest, PAYMENT_ID)).thenReturn(refundResponseMock);
        when(refundResponseMock.getActionId()).thenReturn(ACTION_ID);
        when(checkoutComPaymentTransactionServiceMock.getPaymentReferenceFromTransactionEntryCode(MERCHANT_TRANSACTION_CODE)).thenReturn(PAYMENT_REFERENCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenRequestNull_ShouldThrowException() {
        testObj.perform(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenCurrencyNull_ShouldThrowException() {
        when(refundRequestMock.getCurrency()).thenReturn(null);

        testObj.perform(refundRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenRequestId_ShouldThrowException() {
        when(refundRequestMock.getRequestId()).thenReturn(null);

        testObj.perform(refundRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenTotalAmountNull_ShouldThrowException() {
        when(refundRequestMock.getTotalAmount()).thenReturn(null);

        testObj.perform(refundRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenMerchantTransactionCodeNull_ShouldThrowException() {
        when(refundRequestMock.getMerchantTransactionCode()).thenReturn(null);

        testObj.perform(refundRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void perform_WhenExecutionExceptionWithError500_ShouldThrowPaymentIntegrationException() throws ExecutionException, InterruptedException {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        apiResponseInfo.setHttpStatusCode(503);

        when(checkoutComPaymentIntegrationServiceMock.refundPayment(refundRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new CheckoutApiException(apiResponseInfo)));

        testObj.perform(refundRequestMock);
    }

    @Test
    public void perform_WhenExecutionExceptionWithNonError500_ShouldReturnInvalidRequestErrorRefundResult() throws ExecutionException, InterruptedException {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        apiResponseInfo.setHttpStatusCode(404);

        when(checkoutComPaymentIntegrationServiceMock.refundPayment(refundRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new CheckoutApiException(apiResponseInfo)));

        final RefundResult result = testObj.perform(refundRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.INVALID_REQUEST, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenOtherExecutionException_ShouldReturnInvalidRequestErrorRefundResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.refundPayment(refundRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new NullPointerException()));

        final RefundResult result = testObj.perform(refundRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.INVALID_REQUEST, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenCancellationException_ShouldReturnCommunicationProblemErrorRefundResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.refundPayment(refundRequest, PAYMENT_ID)).thenThrow(new CancellationException());

        final RefundResult result = testObj.perform(refundRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.COMMUNICATION_PROBLEM, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenInterruptedException_ShouldReturnCommunicationProblemErrorRefundResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.refundPayment(refundRequest, PAYMENT_ID)).thenThrow(new InterruptedException());

        final RefundResult result = testObj.perform(refundRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.COMMUNICATION_PROBLEM, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_When_ShouldReturnErrorRefundResult() {
        final RefundResult result = testObj.perform(refundRequestMock);

        assertEquals(TransactionStatus.PENDING, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.SUCCESFULL, result.getTransactionStatusDetails());
        assertEquals(Currency.getInstance(Locale.UK), result.getCurrency());
        assertEquals(ACTION_ID, result.getRequestToken());
        assertEquals(ORDER_TOTAL_PRICE, result.getTotalAmount());
        assertEquals(PAYMENT_ID, result.getRequestId());
        assertEquals(DATE, result.getRequestTime());
    }

}