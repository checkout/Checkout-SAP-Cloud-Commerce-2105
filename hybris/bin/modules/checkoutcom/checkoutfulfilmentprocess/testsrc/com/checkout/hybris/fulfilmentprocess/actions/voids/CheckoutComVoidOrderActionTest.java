package com.checkout.hybris.fulfilmentprocess.actions.voids;

import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.fulfilmentprocess.model.CheckoutComVoidProcessModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.task.RetryLaterException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComVoidOrderActionTest {

    private static final String OK = "OK";
    private static final String NOK = "NOK";
    private static final String WAIT = "WAIT";

    @InjectMocks
    private CheckoutComVoidOrderAction testObj;

    @Mock
    private CheckoutComPaymentService paymentServiceMock;
    @Mock
    private CheckoutComVoidProcessModel checkoutComVoidProcessModelMock;
    @Mock
    private PaymentTransactionEntryModel authTransactionEntryModelMock, voidTransactionEntryModelMock;
    @Mock
    private OrderModel orderModelMock;
    @Mock
    private PaymentTransactionModel paymentTransactionMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    @Before
    public void setUp() {
        when(checkoutComVoidProcessModelMock.getOrder()).thenReturn(orderModelMock);
        when(orderModelMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransactionMock));
        when(paymentServiceMock.isAuthorizationApproved(orderModelMock)).thenReturn(true);
        when(paymentServiceMock.isCapturePending(orderModelMock)).thenReturn(true);
        when(checkoutComPaymentTransactionServiceMock.getPaymentTransaction(orderModelMock)).thenReturn(paymentTransactionMock);
        when(checkoutComPaymentTransactionServiceMock.findAcceptedAuthorizationEntry(paymentTransactionMock)).thenReturn(of(authTransactionEntryModelMock));
        when(paymentServiceMock.isVoidPresent(orderModelMock)).thenReturn(false);
    }

    @Test
    public void getTransitions_ShouldReturnTheTransactions() {
        final Set<String> transitions = testObj.getTransitions();

        assertTrue(transitions.contains(OK));
        assertTrue(transitions.contains(NOK));
        assertTrue(transitions.contains(WAIT));
    }

    @Test
    public void execute_WhenOrderIsNull_ShouldReturnNOK() {
        when(checkoutComVoidProcessModelMock.getOrder()).thenReturn(null);

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(NOK, result);
    }

    @Test
    public void execute_WhenNoTransactions_ShouldReturnNOK() {
        when(orderModelMock.getPaymentTransactions()).thenReturn(emptyList());

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(NOK, result);
    }

    @Test
    public void execute_WhenVoidTransactionEntryIsAlreadyPresent_ShouldReturnOK() {
        when(paymentServiceMock.isVoidPresent(orderModelMock)).thenReturn(true);

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(OK, result);
    }

    @Test
    public void execute_WhenNoAuthTransactionEntryAccepted_ShouldReturnNOK() {
        when(orderModelMock.getPaymentTransactions()).thenReturn(singletonList(paymentTransactionMock));
        when(checkoutComPaymentTransactionServiceMock.findAcceptedAuthorizationEntry(paymentTransactionMock)).thenReturn(empty());

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(NOK, result);
    }

    @Test(expected = RetryLaterException.class)
    public void execute_WhenVoidThrowsPaymentIntegrationException_ShouldThrowRetryLaterException() {
        when(orderModelMock.getPaymentTransactions()).thenReturn(singletonList(paymentTransactionMock));
        when(paymentServiceMock.cancel(authTransactionEntryModelMock)).thenThrow(new CheckoutComPaymentIntegrationException("error"));

        testObj.execute(checkoutComVoidProcessModelMock);
    }

    @Test
    public void execute_WhenVoidIsCorrectlyPerformedAndTransactionEntryIsAccepted_ShouldReturnOK() {
        when(paymentServiceMock.cancel(authTransactionEntryModelMock)).thenReturn(voidTransactionEntryModelMock);
        when(voidTransactionEntryModelMock.getTransactionStatus()).thenReturn(TransactionStatus.ACCEPTED.toString());

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(OK, result);
        verify(paymentServiceMock).cancel(authTransactionEntryModelMock);
    }

    @Test
    public void execute_WhenVoidIsCorrectlyPerformedAndTransactionEntryIsError_ShouldReturnNOK() {
        when(paymentServiceMock.cancel(authTransactionEntryModelMock)).thenReturn(voidTransactionEntryModelMock);
        when(voidTransactionEntryModelMock.getTransactionStatus()).thenReturn(TransactionStatus.ERROR.toString());

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(NOK, result);
        verify(paymentServiceMock).cancel(authTransactionEntryModelMock);
    }

    @Test
    public void execute_WhenVoidIsCorrectlyPerformedAndTransactionEntryIsPending_ShouldReturnWAIT() {
        when(paymentServiceMock.cancel(authTransactionEntryModelMock)).thenReturn(voidTransactionEntryModelMock);
        when(voidTransactionEntryModelMock.getTransactionStatus()).thenReturn(TransactionStatus.PENDING.toString());

        final String result = testObj.execute(checkoutComVoidProcessModelMock);

        assertEquals(WAIT, result);
        verify(paymentServiceMock).cancel(authTransactionEntryModelMock);
    }
}
