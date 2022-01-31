package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.impl.CheckoutComMultibancoPaymentResponseStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.payments.CardSourceResponse;
import com.checkout.payments.PaymentPending;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.order.CommercePaymentProviderStrategy;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MULTIBANCO;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_APPROVED;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_PENDING;
import static de.hybris.platform.payment.dto.TransactionStatus.*;
import static de.hybris.platform.payment.dto.TransactionStatusDetails.*;
import static de.hybris.platform.payment.enums.PaymentTransactionType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentServiceTest {

    private static final String SITE_ID = "siteId";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final Date CURRENT_DATE = new Date();
    private static final String PAYMENT_REFERENCE = "PAYMENT-REFERENCE";
    private static final String PROVIDER = "PROVIDER";
    private static final BigDecimal AMOUNT = new BigDecimal(2000d);
    private static final String ACTION_ID = "actionId";
    private static final String SUBSCRIPTION_ID = "subscriptionID";
    private static final String USER_ID = "USER_ID";
    private static final String PAYMENT_1_CODE = "payment1Code";
    private static final String PAYMENT_2_CODE = "payment2Code";

    @Spy
    @InjectMocks
    private DefaultCheckoutComPaymentService testObj;

    @Mock
    private TimeService timeServiceMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel cardPaymentInfoMock, userPaymentInfo1Mock, userPaymentInfo2Mock;
    @Mock
    private PaymentTransactionModel paymentTransaction1Mock;
    @Mock
    private CommercePaymentProviderStrategy commercePaymentProviderStrategyMock;
    @Mock
    private PaymentTransactionEntryModel paymentTransactionEntryMock, capturePaymentTransactionEntryMock,
            capturePendingPaymentTransactionEntryMock, rejectedAuthorizationPaymentTransactionEntryMock,
            acceptedAuthorizationPaymentTransactionEntryMock, reviewAuthorizationPaymentTransactionEntryMock,
            refundPaymentTransactionEntry1Mock, refundPaymentTransactionEntry2Mock,
            cancelPaymentTransactionEntryMock, authorizationPendingPaymentTransactionEntryMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComPaymentEventModel paymentEventMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolverMock;
    @Mock
    private CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapperMock;
    @Mock
    private PaymentPending paymentPendingMock;
    @Mock
    private CheckoutComMultibancoPaymentResponseStrategy checkoutComMultibancoPaymentResponseStrategyMock;
    @Mock
    private AuthorizeResponse authorizeResponseMock;
    @Mock
    private CardSourceResponse sourceMock;
    @Mock
    private CustomerModel userMock;

    @Before
    public void setUp() {
        setUpTestObjMocks();
        setUpPaymentEvent();
        setUpPaymentTransactionsAndTransactionEntries();

        when(timeServiceMock.getCurrentTime()).thenReturn(CURRENT_DATE);
        when(orderMock.getCurrency()).thenReturn(currencyModelMock);
        when(orderMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(orderMock.getPaymentTransactions()).thenReturn(singletonList(paymentTransaction1Mock));
        when(commercePaymentProviderStrategyMock.getPaymentProvider()).thenReturn(PROVIDER);
        when(modelServiceMock.create(PaymentTransactionModel.class)).thenReturn(new PaymentTransactionModel());
        when(modelServiceMock.create(PaymentTransactionEntryModel.class)).thenReturn(new PaymentTransactionEntryModel());
        when(currencyModelMock.getIsocode()).thenReturn("GBP");
        when(cardPaymentInfoMock.getCode()).thenReturn(PAYMENT_1_CODE);
        when(cartMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(cartMock.getUser()).thenReturn(userMock);
        when(userMock.getUid()).thenReturn(USER_ID);
        when(userMock.getPaymentInfos()).thenReturn(ImmutableList.of(userPaymentInfo1Mock, userPaymentInfo2Mock));
        when(userPaymentInfo1Mock.getCode()).thenReturn(PAYMENT_1_CODE);
        when(userPaymentInfo2Mock.getCode()).thenReturn(PAYMENT_2_CODE);
        when(cardPaymentInfoMock.getCode()).thenReturn(PAYMENT_1_CODE);
        when(cardPaymentInfoMock.getUser()).thenReturn(userMock);
        when(cartMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(sourceMock.getId()).thenReturn(SUBSCRIPTION_ID);
        when(paymentTransaction1Mock.getOrder()).thenReturn(orderMock);
        when(orderMock.getSite().getUid()).thenReturn(SITE_ID);
    }

    @Test
    public void isAuthorizationApproved_WhenNoPaymentTransactions_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(null);

        assertFalse(testObj.isAuthorizationApproved(orderMock));
    }

    @Test
    public void isAuthorizationApproved_WhenNoAuthorisationPaymentTransactionEntries_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));

        assertFalse(testObj.isAuthorizationApproved(orderMock));
    }

    @Test
    public void isAuthorizationApproved_WhenNotAcceptedAuthorisationPaymentTransactionEntry_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, rejectedAuthorizationPaymentTransactionEntryMock));

        assertFalse(testObj.isAuthorizationApproved(orderMock));
    }

    @Test
    public void isAuthorizationApproved_WhenAcceptedAuthorisationPaymentTransactionEntry_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, acceptedAuthorizationPaymentTransactionEntryMock));

        assertTrue(testObj.isAuthorizationApproved(orderMock));
    }

    @Test
    public void isAuthorizationApproved_WhenReviewAuthorisationPaymentTransactionEntry_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, reviewAuthorizationPaymentTransactionEntryMock));

        assertTrue(testObj.isAuthorizationApproved(orderMock));
    }

    @Test
    public void isAuthorizationPending_WhenNoTransaction_ShouldReturnTrue() {
        final boolean result = testObj.isAuthorizationPending(orderMock);

        assertTrue(result);
    }

    @Test
    public void isAuthorizationPending_WhenNoTransactionEntry_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.isAuthorizationPending(orderMock);

        assertTrue(result);
    }

    @Test
    public void isAuthorizationPending_WhenNoAuthTransactionEntryAccepted_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransaction1Mock));
        when(paymentTransaction1Mock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryMock));
        when(paymentTransactionEntryMock.getType()).thenReturn(CAPTURE);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(ACCEPTED.toString());

        final boolean result = testObj.isAuthorizationPending(orderMock);

        assertTrue(result);
    }

    @Test
    public void isAuthorizationPending_WhenAcceptedAuthTransactionEntry_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransaction1Mock));
        when(paymentTransaction1Mock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryMock));
        when(paymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(ACCEPTED.toString());

        final boolean result = testObj.isAuthorizationPending(orderMock);

        assertFalse(result);
    }

    @Test
    public void isAuthorizationPending_WhenPendingAuthTransactionEntry_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransaction1Mock));
        when(paymentTransaction1Mock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryMock));
        when(paymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(PENDING.toString());

        final boolean result = testObj.isAuthorizationPending(orderMock);

        assertTrue(result);
    }

    @Test
    public void isCapturePending_WhenNoTransaction_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.isCapturePending(orderMock);

        assertTrue(result);
    }

    @Test
    public void isCapturePending_WhenNoTransactionEntry_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.isCapturePending(orderMock);

        assertTrue(result);
    }

    @Test
    public void isCaptureApproved_WhenNoTransactions_ShouldReturnFalse() {
        final boolean result = testObj.isCaptureApproved(orderMock);

        assertFalse(result);
    }

    @Test
    public void isCaptureApproved_WhenNoTransactionEntry_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.isCaptureApproved(orderMock);

        assertFalse(result);
    }

    @Test
    public void isCaptureApproved_WhenNoCaptureTransactionEntryAccepted_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransaction1Mock));
        when(paymentTransaction1Mock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryMock));
        when(paymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(ACCEPTED.toString());

        final boolean result = testObj.isCaptureApproved(orderMock);

        assertFalse(result);
    }

    @Test
    public void isCaptureApproved_WhenCaptureTransactionEntryAccepted_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(Collections.singletonList(paymentTransaction1Mock));
        when(paymentTransaction1Mock.getEntries()).thenReturn(Collections.singletonList(paymentTransactionEntryMock));
        when(paymentTransactionEntryMock.getType()).thenReturn(CAPTURE);
        when(paymentTransactionEntryMock.getTransactionStatus()).thenReturn(ACCEPTED.toString());

        final boolean result = testObj.isCaptureApproved(orderMock);

        assertTrue(result);
    }

    @Test
    public void isAutoCapture_WhenOrderNull_ReturnFalse() {
        final boolean result = testObj.isAutoCapture(null);

        assertFalse(result);
    }

    @Test
    public void isAutoCapture_WhenPaymentInfoNull_ReturnFalse() {
        final boolean result = testObj.isAutoCapture(orderMock);

        assertFalse(result);
    }

    @Test
    public void isAutoCapture_WhenPaymentInfoNotAutoCapture_ReturnFalse() {
        when(orderMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(cardPaymentInfoMock.getAutoCapture()).thenReturn(false);

        final boolean result = testObj.isAutoCapture(orderMock);

        assertFalse(result);
    }

    @Test
    public void isAutoCapture_WhenPaymentInfoIsAutoCapture_ReturnTrue() {
        when(orderMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(cardPaymentInfoMock.getAutoCapture()).thenReturn(true);

        final boolean result = testObj.isAutoCapture(orderMock);

        assertTrue(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void captureExists_WhenTransactionIsNull_ShouldThrowException() {
        testObj.captureExists(null);
    }

    @Test
    public void captureExists_WhenThereAreNotTransactionEntries_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(emptyList());

        assertFalse(testObj.captureExists(orderMock));
    }

    @Test
    public void captureExists_WhenTransactionEntryCaptureDoesNotExist_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(rejectedAuthorizationPaymentTransactionEntryMock));

        assertFalse(testObj.captureExists(orderMock));
    }

    @Test
    public void captureExists_WhenTransactionEntryCaptureExists_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, reviewAuthorizationPaymentTransactionEntryMock));

        assertTrue(testObj.captureExists(orderMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isCapturePending_WhenOrderIsNull_ShouldThrowException() {
        testObj.isCapturePending(null);
    }

    @Test
    public void isCapturePending_WhenThereAreNotTransactionEntries_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(emptyList());

        assertTrue(testObj.isCapturePending(orderMock));
    }

    @Test
    public void isCapturePending_WhenTransactionEntryCaptureDoesNotExist_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(rejectedAuthorizationPaymentTransactionEntryMock));

        assertTrue(testObj.isCapturePending(orderMock));
    }

    @Test
    public void isCapturePending_WhenTransactionEntryCaptureExists_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePendingPaymentTransactionEntryMock));

        assertFalse(testObj.isCapturePending(orderMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isVoidPresent_WhenNullOrder_ShouldThrowException() {
        testObj.isVoidPresent(null);
    }

    @Test
    public void isVoidPresent_WhenNoPaymentTransactions_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(null);

        assertFalse(testObj.isVoidPresent(orderMock));
    }

    @Test
    public void isVoidPresent_WhenNoPaymentTransactionEntries_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(emptyList());

        assertFalse(testObj.isVoidPresent(orderMock));
    }

    @Test
    public void isVoidPresent_WhenNoCancelPaymentTransaction_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));

        assertFalse(testObj.isVoidPresent(orderMock));
    }

    @Test
    public void isVoidPresent_WhenCancelPaymentTransactionPresent_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(acceptedAuthorizationPaymentTransactionEntryMock, capturePaymentTransactionEntryMock, cancelPaymentTransactionEntryMock));

        assertTrue(testObj.isVoidPresent(orderMock));
    }


    @Test(expected = IllegalArgumentException.class)
    public void isVoidPending_WhenNullOrder_ShouldThrowException() {
        testObj.isVoidPending(null);
    }

    @Test
    public void isVoidPending_WhenNoPaymentTransactions_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(null);

        assertFalse(testObj.isVoidPending(orderMock));
    }

    @Test
    public void isVoidPending_WhenNoPaymentTransactionEntries_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(emptyList());

        assertFalse(testObj.isVoidPending(orderMock));
    }

    @Test
    public void isVoidPending_WhenNoCancelPaymentTransaction_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));

        assertFalse(testObj.isVoidPending(orderMock));
    }

    @Test
    public void isVoidPending_WhenCancelPaymentNoTransactionPending_ShouldReturnFalse() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(acceptedAuthorizationPaymentTransactionEntryMock, capturePaymentTransactionEntryMock, cancelPaymentTransactionEntryMock));
        when(cancelPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(ACCEPTED.name());

        assertFalse(testObj.isVoidPending(orderMock));
    }

    @Test
    public void isVoidPending_WhenCancelPaymentTransactionPending_ShouldReturnTrue() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(acceptedAuthorizationPaymentTransactionEntryMock, capturePaymentTransactionEntryMock, cancelPaymentTransactionEntryMock));
        when(cancelPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(PENDING.name());

        assertTrue(testObj.isVoidPending(orderMock));
    }

    @Test
    public void findPendingTransactionEntry_WhenNoTransactionEntryForType_ShouldReturnEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(refundPaymentTransactionEntry1Mock));
        when(refundPaymentTransactionEntry1Mock.getRequestId()).thenReturn(PAYMENT_ID);

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingTransactionEntry(PAYMENT_ID, paymentTransaction1Mock, CAPTURE);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findPendingTransactionEntry_WhenTransactionEntryForTypeWithDifferentPaymentId_ShouldReturnEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));
        when(capturePaymentTransactionEntryMock.getRequestId()).thenReturn("somePaymentId");

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingTransactionEntry(PAYMENT_ID, paymentTransaction1Mock, CAPTURE);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findPendingTransactionEntry_WhenTransactionEntryForTypeWithSamePaymentIdButNotPending_ShouldReturnEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));
        when(capturePaymentTransactionEntryMock.getRequestId()).thenReturn(PAYMENT_ID);
        when(capturePaymentTransactionEntryMock.getTransactionStatus()).thenReturn(ACCEPTED.toString());

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingTransactionEntry(PAYMENT_ID, paymentTransaction1Mock, CAPTURE);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findPendingTransactionEntry_WhenPendingTransactionEntryForTypeWithSamePaymentId_ShouldReturnTransactionEntry() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));
        when(capturePaymentTransactionEntryMock.getRequestId()).thenReturn(PAYMENT_ID);
        when(capturePaymentTransactionEntryMock.getTransactionStatus()).thenReturn(PENDING.toString());

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingTransactionEntry(PAYMENT_ID, paymentTransaction1Mock, CAPTURE);

        assertTrue(result.isPresent());
        assertSame(capturePaymentTransactionEntryMock, result.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void acceptPayment_WhenNullEvent_ShouldThrowException() {
        testObj.acceptPayment(null, paymentTransaction1Mock, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void acceptPayment_WhenNullTransaction_ShouldThrowException() {
        testObj.acceptPayment(paymentEventMock, null, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void acceptPayment_WhenNullTransactionType_ShouldThrowException() {
        testObj.acceptPayment(paymentEventMock, paymentTransaction1Mock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectPayment_WhenNullPaymentEvent_ShouldThrowException() {
        testObj.rejectPayment(null, paymentTransaction1Mock, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectPayment_WhenNullPaymentTransaction_ShouldThrowException() {
        testObj.rejectPayment(paymentEventMock, null, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectPayment_WhenNullTransactionType_ShouldThrowException() {
        testObj.rejectPayment(paymentEventMock, paymentTransaction1Mock, null);
    }

    @Test
    public void handlePendingPaymentResponse_WhenMultibancoPaymentMethod_ShouldUseMultibancoStrategyAndReturnAuthoriseResponse() {
        when(checkoutComPaymentTypeResolverMock.resolvePaymentType(apmPaymentInfoMock)).thenReturn(MULTIBANCO);
        when(checkoutComPaymentResponseStrategyMapperMock.findStrategy(MULTIBANCO)).thenReturn(checkoutComMultibancoPaymentResponseStrategyMock);
        when(checkoutComMultibancoPaymentResponseStrategyMock.handlePendingPaymentResponse(paymentPendingMock, apmPaymentInfoMock)).thenReturn(authorizeResponseMock);

        final AuthorizeResponse result = testObj.handlePendingPaymentResponse(paymentPendingMock, apmPaymentInfoMock);

        assertEquals(authorizeResponseMock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenPaymentResponseIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(null, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handlePendingPaymentResponse_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.handlePendingPaymentResponse(paymentPendingMock, null);
    }

    @Test
    public void isDeferred_WhenApmPaymentInfoIsDeferred_ShouldReturnTrue() {
        when(orderMock.getPaymentInfo()).thenReturn(apmPaymentInfoMock);
        when(apmPaymentInfoMock.getDeferred()).thenReturn(true);

        assertTrue(testObj.isDeferred(orderMock));
    }

    @Test
    public void isDeferred_WhenApmPaymentInfoIsNotDeferred_ShouldReturnFalse() {
        when(orderMock.getPaymentInfo()).thenReturn(apmPaymentInfoMock);
        when(apmPaymentInfoMock.getDeferred()).thenReturn(false);

        assertFalse(testObj.isDeferred(orderMock));
    }

    @Test
    public void isDeferred_WhenCardPaymentInfo_ShouldReturnFalse() {
        assertFalse(testObj.isDeferred(orderMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isDeferred_WhenPaymentInfoIsNull_ShouldThrowException() {
        when(orderMock.getPaymentInfo()).thenReturn(null);

        assertFalse(testObj.isDeferred(orderMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isDeferred_WhenOrderIsNull_ShouldThrowException() {
        testObj.isDeferred(null);
    }

    @Test
    public void acceptPayment_WhenPaymentTransactionEntryExists_ShouldUpdateStatusToAccepted() {
        when(paymentEventMock.getPaymentId()).thenReturn(PAYMENT_ID);
        doReturn(Optional.of(paymentTransactionEntryMock)).when(testObj).findPendingTransactionEntry(PAYMENT_ID, paymentTransaction1Mock, AUTHORIZATION);

        testObj.acceptPayment(paymentEventMock, paymentTransaction1Mock, AUTHORIZATION);

        verify(paymentTransactionEntryMock).setTransactionStatus(ACCEPTED.name());
        verify(modelServiceMock).save(paymentTransactionEntryMock);
        verify(checkoutComPaymentTransactionServiceMock, never()).createPaymentTransactionEntry(any(PaymentTransactionModel.class), any(CheckoutComPaymentEventModel.class), anyString(), anyString(), any(PaymentTransactionType.class));
    }

    @Test
    public void acceptPayment_WhenPaymentTransactionEntryDoesNotExistAndEventWithNoRisk_ShouldCreateNewAcceptedEntry() {
        when(paymentEventMock.getRiskFlag()).thenReturn(Boolean.FALSE);
        doNothing().when(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(any(PaymentTransactionModel.class), any(CheckoutComPaymentEventModel.class), anyString(), anyString(), any(PaymentTransactionType.class));

        testObj.acceptPayment(paymentEventMock, paymentTransaction1Mock, AUTHORIZATION);

        verify(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED.name(), SUCCESFULL.name(), AUTHORIZATION);
    }

    @Test
    public void acceptPayment_WhenPaymentTransactionEntryDoesNotExistAndEventIsPaymentPending_ShouldCreateNewPendingEntry() {
        when(paymentEventMock.getRiskFlag()).thenReturn(Boolean.FALSE);
        when(paymentEventMock.getEventType()).thenReturn(PAYMENT_PENDING.getCode());
        doNothing().when(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(any(PaymentTransactionModel.class), any(CheckoutComPaymentEventModel.class), anyString(), anyString(), any(PaymentTransactionType.class));

        testObj.acceptPayment(paymentEventMock, paymentTransaction1Mock, AUTHORIZATION);

        verify(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, PENDING.name(), SUCCESFULL.name(), AUTHORIZATION);
    }

    @Test
    public void acceptPayment_WhenPaymentTransactionEntryDoesNotExistAndEventWithRiskWithMerchantIgnoringRisk_ShouldCreateNewAcceptedEntry() {
        when(paymentEventMock.getRiskFlag()).thenReturn(Boolean.TRUE);
        when(checkoutComMerchantConfigurationServiceMock.isReviewTransactionsAtRisk(SITE_ID)).thenReturn(false);
        doNothing().when(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(any(PaymentTransactionModel.class), any(CheckoutComPaymentEventModel.class), anyString(), anyString(), any(PaymentTransactionType.class));

        testObj.acceptPayment(paymentEventMock, paymentTransaction1Mock, AUTHORIZATION);

        verify(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED.name(), SUCCESFULL.name(), AUTHORIZATION);
    }

    @Test
    public void acceptPayment_WhenPaymentTransactionEntryDoesNotExistAndEventWithRiskWithMerchantNotIgnoringRisk_ShouldCreateNewReviewEntry() {
        when(paymentEventMock.getRiskFlag()).thenReturn(Boolean.TRUE);
        when(checkoutComMerchantConfigurationServiceMock.isReviewTransactionsAtRisk(SITE_ID)).thenReturn(true);
        doNothing().when(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(any(PaymentTransactionModel.class), any(CheckoutComPaymentEventModel.class), anyString(), anyString(), any(PaymentTransactionType.class));

        testObj.acceptPayment(paymentEventMock, paymentTransaction1Mock, AUTHORIZATION);

        verify(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, REVIEW.name(), REVIEW_NEEDED.name(), AUTHORIZATION);
    }

    @Test
    public void rejectPayment_ShouldCreateRejectedPaymentTransactionEntryForApprovedEventType() {
        doNothing().when(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(any(PaymentTransactionModel.class), any(CheckoutComPaymentEventModel.class), anyString(), anyString(), any(PaymentTransactionType.class));

        testObj.rejectPayment(paymentEventMock, paymentTransaction1Mock, AUTHORIZATION);

        verify(checkoutComPaymentTransactionServiceMock).createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, REJECTED.name(), PROCESSOR_DECLINE.name(), AUTHORIZATION);
    }

    private void setUpTestObjMocks() {
        ReflectionTestUtils.setField(testObj, "modelService", modelServiceMock);
        ReflectionTestUtils.setField(testObj, "checkoutComMerchantConfigurationService", checkoutComMerchantConfigurationServiceMock);
        ReflectionTestUtils.setField(testObj, "checkoutComPaymentResponseStrategyMapper", checkoutComPaymentResponseStrategyMapperMock);
        ReflectionTestUtils.setField(testObj, "checkoutComPaymentTypeResolver", checkoutComPaymentTypeResolverMock);
        ReflectionTestUtils.setField(testObj, "checkoutComPaymentTransactionService", checkoutComPaymentTransactionServiceMock);
    }

    private void setUpPaymentEvent() {
        when(paymentEventMock.getActionId()).thenReturn(ACTION_ID);
        when(paymentEventMock.getCurrency()).thenReturn(currencyModelMock);
        when(paymentEventMock.getAmount()).thenReturn(AMOUNT);
        when(paymentEventMock.getEventType()).thenReturn(PAYMENT_APPROVED.toString());
    }

    private void setUpPaymentTransactionsAndTransactionEntries() {
        when(paymentTransaction1Mock.getCode()).thenReturn(PAYMENT_REFERENCE);
        when(paymentTransaction1Mock.getRequestId()).thenReturn(PAYMENT_ID);
        when(capturePaymentTransactionEntryMock.getType()).thenReturn(CAPTURE);
        when(capturePendingPaymentTransactionEntryMock.getType()).thenReturn(CAPTURE);
        when(capturePendingPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(PENDING.toString());
        when(authorizationPendingPaymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(authorizationPendingPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(PENDING.toString());
        when(rejectedAuthorizationPaymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(rejectedAuthorizationPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.REJECTED.toString());
        when(acceptedAuthorizationPaymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(acceptedAuthorizationPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.ACCEPTED.toString());
        when(acceptedAuthorizationPaymentTransactionEntryMock.getTransactionStatusDetails()).thenReturn(SUCCESFULL.toString());
        when(reviewAuthorizationPaymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(reviewAuthorizationPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(TransactionStatus.REVIEW.toString());
        when(refundPaymentTransactionEntry1Mock.getType()).thenReturn(REFUND_FOLLOW_ON);
        when(refundPaymentTransactionEntry2Mock.getType()).thenReturn(REFUND_FOLLOW_ON);
        when(cancelPaymentTransactionEntryMock.getType()).thenReturn(CANCEL);
        when(acceptedAuthorizationPaymentTransactionEntryMock.getCurrency()).thenReturn(currencyModelMock);
        when(checkoutComPaymentTransactionServiceMock.getPaymentTransaction(orderMock)).thenReturn(paymentTransaction1Mock);
    }
}