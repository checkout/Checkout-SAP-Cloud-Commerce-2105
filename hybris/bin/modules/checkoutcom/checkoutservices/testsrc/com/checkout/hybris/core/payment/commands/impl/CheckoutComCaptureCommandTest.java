package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.CheckoutApiException;
import com.checkout.common.ApiResponseInfo;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.payments.CaptureResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.commands.request.CaptureRequest;
import de.hybris.platform.payment.commands.result.CaptureResult;
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
public class CheckoutComCaptureCommandTest {

    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String AMOUNT = "100";
    private static final String MERCHANT_TRANSACTION_CODE = "ORDER-REFERENCE-1-SOMETHING";
    private static final String PAYMENT_REFERENCE = "ORDER-REFERENCE";
    private static final Date DATE = new Date();
    private static final String ACTION_ID = "Action_id";
    private static final BigDecimal ORDER_TOTAL_PRICE = new BigDecimal("100");

    @InjectMocks
    private CheckoutComCaptureCommand testObj;

    @Mock
    private CheckoutComRequestFactory checkoutComRequestFactoryMock;
    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private CaptureRequest captureRequestMock;
    @Mock
    private com.checkout.payments.CaptureRequest captureRequest;
    @Mock
    private CaptureResponse captureResponseMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        final Currency currency = Currency.getInstance(Locale.UK);
        when(captureRequestMock.getCurrency()).thenReturn(currency);
        when(captureRequestMock.getRequestId()).thenReturn(PAYMENT_ID);
        when(captureRequestMock.getTotalAmount()).thenReturn(new BigDecimal(AMOUNT));
        when(captureRequestMock.getMerchantTransactionCode()).thenReturn(MERCHANT_TRANSACTION_CODE);
        when(checkoutComRequestFactoryMock.createCapturePaymentRequest(new BigDecimal(AMOUNT), PAYMENT_REFERENCE, currency.getCurrencyCode())).thenReturn(captureRequest);
        when(timeServiceMock.getCurrentTime()).thenReturn(DATE);
        when(captureRequest.getAmount()).thenReturn(10000l);
        when(captureRequest.getReference()).thenReturn(PAYMENT_REFERENCE);
        when(checkoutComPaymentIntegrationServiceMock.capturePayment(captureRequest, PAYMENT_ID)).thenReturn(captureResponseMock);
        when(captureResponseMock.getActionId()).thenReturn(ACTION_ID);
        when(checkoutComPaymentTransactionServiceMock.getPaymentReferenceFromTransactionEntryCode(MERCHANT_TRANSACTION_CODE)).thenReturn(PAYMENT_REFERENCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenRequestNull_ShouldThrowException() {
        testObj.perform(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenCurrencyNull_ShouldThrowException() {
        when(captureRequestMock.getCurrency()).thenReturn(null);

        testObj.perform(captureRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenRequestId_ShouldThrowException() {
        when(captureRequestMock.getRequestId()).thenReturn(null);

        testObj.perform(captureRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenTotalAmountNull_ShouldThrowException() {
        when(captureRequestMock.getTotalAmount()).thenReturn(null);

        testObj.perform(captureRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenMerchantTransactionCodeNull_ShouldThrowException() {
        when(captureRequestMock.getMerchantTransactionCode()).thenReturn(null);

        testObj.perform(captureRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void perform_WhenExecutionExceptionWithError500_ShouldThrowPaymentIntegrationException() throws ExecutionException, InterruptedException {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        apiResponseInfo.setHttpStatusCode(503);

        when(checkoutComPaymentIntegrationServiceMock.capturePayment(captureRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new CheckoutApiException(apiResponseInfo)));

        testObj.perform(captureRequestMock);
    }

    @Test
    public void perform_WhenExecutionExceptionWithNonError500_ShouldReturnInvalidRequestErrorCaptureResult() throws ExecutionException, InterruptedException {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        apiResponseInfo.setHttpStatusCode(404);

        when(checkoutComPaymentIntegrationServiceMock.capturePayment(captureRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new CheckoutApiException(apiResponseInfo)));

        final CaptureResult result = testObj.perform(captureRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.INVALID_REQUEST, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenOtherExecutionException_ShouldReturnInvalidRequestErrorCaptureResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.capturePayment(captureRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new NullPointerException()));

        final CaptureResult result = testObj.perform(captureRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.INVALID_REQUEST, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenCancellationException_ShouldReturnCommunicationProblemErrorCaptureResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.capturePayment(captureRequest, PAYMENT_ID)).thenThrow(new CancellationException());

        final CaptureResult result = testObj.perform(captureRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.COMMUNICATION_PROBLEM, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenInterruptedException_ShouldReturnCommunicationProblemErrorCaptureResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.capturePayment(captureRequest, PAYMENT_ID)).thenThrow(new InterruptedException());

        final CaptureResult result = testObj.perform(captureRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.COMMUNICATION_PROBLEM, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_When_ShouldReturnErrorCaptureResult() {
        final CaptureResult result = testObj.perform(captureRequestMock);

        assertEquals(TransactionStatus.PENDING, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.SUCCESFULL, result.getTransactionStatusDetails());
        assertEquals(Currency.getInstance(Locale.UK), result.getCurrency());
        assertEquals(ACTION_ID, result.getRequestToken());
        assertEquals(ORDER_TOTAL_PRICE, result.getTotalAmount());
        assertEquals(PAYMENT_ID, result.getRequestId());
        assertEquals(DATE, result.getRequestTime());
    }
}
