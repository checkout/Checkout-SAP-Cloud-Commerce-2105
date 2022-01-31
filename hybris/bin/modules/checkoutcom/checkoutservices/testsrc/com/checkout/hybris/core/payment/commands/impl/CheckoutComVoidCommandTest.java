package com.checkout.hybris.core.payment.commands.impl;

import com.checkout.CheckoutApiException;
import com.checkout.common.ApiResponseInfo;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.payments.VoidResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.commands.request.VoidRequest;
import de.hybris.platform.payment.commands.result.VoidResult;
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
public class CheckoutComVoidCommandTest {

    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String AMOUNT = "100";
    private static final String MERCHANT_TRANSACTION_CODE = "ORDER-REFERENCE-1-SOMETHING";
    private static final String PAYMENT_REFERENCE = "ORDER-REFERENCE";
    private static final Date DATE = new Date();
    private static final String ACTION_ID = "Action_id";

    @InjectMocks
    private CheckoutComVoidCommand testObj;

    @Mock
    private CheckoutComRequestFactory checkoutComRequestFactoryMock;
    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private VoidRequest voidRequestMock;
    @Mock
    private com.checkout.payments.VoidRequest voidRequest;
    @Mock
    private VoidResponse voidResponseMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        final Currency currency = Currency.getInstance(Locale.UK);
        when(voidRequestMock.getCurrency()).thenReturn(currency);
        when(voidRequestMock.getRequestId()).thenReturn(PAYMENT_ID);
        when(voidRequestMock.getTotalAmount()).thenReturn(new BigDecimal(AMOUNT));
        when(voidRequestMock.getMerchantTransactionCode()).thenReturn(MERCHANT_TRANSACTION_CODE);
        when(checkoutComRequestFactoryMock.createVoidPaymentRequest(PAYMENT_REFERENCE)).thenReturn(voidRequest);
        when(timeServiceMock.getCurrentTime()).thenReturn(DATE);
        when(voidRequest.getReference()).thenReturn(PAYMENT_REFERENCE);
        when(checkoutComPaymentIntegrationServiceMock.voidPayment(voidRequest, PAYMENT_ID)).thenReturn(voidResponseMock);
        when(voidResponseMock.getActionId()).thenReturn(ACTION_ID);
        when(checkoutComPaymentTransactionServiceMock.getPaymentReferenceFromTransactionEntryCode(MERCHANT_TRANSACTION_CODE)).thenReturn(PAYMENT_REFERENCE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenRequestNull_ShouldThrowException() {
        testObj.perform(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenCurrencyNull_ShouldThrowException() {
        when(voidRequestMock.getCurrency()).thenReturn(null);

        testObj.perform(voidRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenRequestId_ShouldThrowException() {
        when(voidRequestMock.getRequestId()).thenReturn(null);

        testObj.perform(voidRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenTotalAmountNull_ShouldThrowException() {
        when(voidRequestMock.getTotalAmount()).thenReturn(null);

        testObj.perform(voidRequestMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void perform_WhenMerchantTransactionCodeNull_ShouldThrowException() {
        when(voidRequestMock.getMerchantTransactionCode()).thenReturn(null);

        testObj.perform(voidRequestMock);
    }

    @Test(expected = CheckoutComPaymentIntegrationException.class)
    public void perform_WhenExecutionExceptionWithError500_ShouldThrowPaymentIntegrationException() throws ExecutionException, InterruptedException {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        apiResponseInfo.setHttpStatusCode(503);

        when(checkoutComPaymentIntegrationServiceMock.voidPayment(voidRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new CheckoutApiException(apiResponseInfo)));

        testObj.perform(voidRequestMock);
    }

    @Test
    public void perform_WhenExecutionExceptionWithNonError500_ShouldReturnInvalidRequestErrorVoidResult() throws ExecutionException, InterruptedException {
        final ApiResponseInfo apiResponseInfo = new ApiResponseInfo();
        apiResponseInfo.setHttpStatusCode(404);

        when(checkoutComPaymentIntegrationServiceMock.voidPayment(voidRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new CheckoutApiException(apiResponseInfo)));

        final VoidResult result = testObj.perform(voidRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.INVALID_REQUEST, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenOtherExecutionException_ShouldReturnInvalidRequestErrorVoidResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.voidPayment(voidRequest, PAYMENT_ID)).thenThrow(new ExecutionException(new NullPointerException()));

        final VoidResult result = testObj.perform(voidRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.INVALID_REQUEST, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenCancellationException_ShouldReturnCommunicationProblemErrorVoidResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.voidPayment(voidRequest, PAYMENT_ID)).thenThrow(new CancellationException());

        final VoidResult result = testObj.perform(voidRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.COMMUNICATION_PROBLEM, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_WhenInterruptedException_ShouldReturnCommunicationProblemErrorVoidResult() throws ExecutionException, InterruptedException {
        when(checkoutComPaymentIntegrationServiceMock.voidPayment(voidRequest, PAYMENT_ID)).thenThrow(new InterruptedException());

        final VoidResult result = testObj.perform(voidRequestMock);

        assertEquals(TransactionStatus.ERROR, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.COMMUNICATION_PROBLEM, result.getTransactionStatusDetails());
    }

    @Test
    public void perform_When_ShouldReturnErrorVoidResult() {
        final VoidResult result = testObj.perform(voidRequestMock);

        assertEquals(TransactionStatus.PENDING, result.getTransactionStatus());
        assertEquals(TransactionStatusDetails.SUCCESFULL, result.getTransactionStatusDetails());
        assertEquals(Currency.getInstance(Locale.UK), result.getCurrency());
        assertEquals(ACTION_ID, result.getRequestToken());
        assertEquals(PAYMENT_ID, result.getRequestId());
        assertEquals(DATE, result.getRequestTime());
    }
}
