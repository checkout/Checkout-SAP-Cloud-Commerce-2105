package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.payments.CardSourceResponse;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.order.CommercePaymentProviderStrategy;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_APPROVED;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.PAYMENT_PENDING;
import static de.hybris.platform.payment.dto.TransactionStatus.PENDING;
import static de.hybris.platform.payment.dto.TransactionStatusDetails.SUCCESFULL;
import static de.hybris.platform.payment.enums.PaymentTransactionType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentTransactionServiceTest {

    private static final String USER_ID = "USER_ID";
    private static final String SUBSCRIPTION_ID = "subscriptionID";
    private static final double ORDER_TOTAL = 10.0d;
    private static final String SITE_ID = "siteId";
    private static final double THRESHOLD = 0.05d;
    private static final String REQUEST_TOKEN = "REQUEST_TOKEN";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final Date CURRENT_DATE = new Date();
    private static final String PAYMENT_REFERENCE = "PAYMENT-REFERENCE";
    private static final String PROVIDER = "PROVIDER";
    private static final BigDecimal AMOUNT = new BigDecimal(2000d);
    private static final String ACCEPTED_PAYMENT_STATUS = TransactionStatus.ACCEPTED.toString();
    private static final String SUCCESSFUL_TRANSACTION_STATUS_DETAILS = SUCCESFULL.toString();
    private static final String PAYMENT_TRANSACTION_ENTRY_CODE = "PAYMENT-REFERENCE-AUTHORIZATION-1";
    private static final String ACTION_ID = "actionId";
    private static final String PAYMENT_1_CODE = "payment1Code";
    private static final String PAYMENT_2_CODE = "payment2Code";

    @Spy
    @InjectMocks
    private DefaultCheckoutComPaymentTransactionService testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private CartModel cartMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private TimeService timeServiceMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel cardPaymentInfoMock, userPaymentInfo1Mock, userPaymentInfo2Mock;
    @Mock
    private PaymentTransactionModel paymentTransaction1Mock, paymentTransaction2Mock;
    @Mock
    private CommercePaymentProviderStrategy commercePaymentProviderStrategyMock;
    @Mock
    private PaymentTransactionEntryModel authorisationEntryMock, captureEntryMock, capturePaymentTransactionEntryMock,
            capturePendingPaymentTransactionEntryMock, rejectedAuthorizationPaymentTransactionEntryMock,
            acceptedAuthorizationPaymentTransactionEntryMock, reviewAuthorizationPaymentTransactionEntryMock,
            refundPaymentTransactionEntry1Mock, refundPaymentTransactionEntry2Mock,
            cancelPaymentTransactionEntryMock, authorizationPendingPaymentTransactionEntryMock;
    @Mock
    private CheckoutComPaymentEventModel paymentEventMock;
    @Captor
    private ArgumentCaptor<PaymentTransactionEntryModel> paymentTransactionEntryModelCaptor;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
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
        when(authorisationEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(captureEntryMock.getType()).thenReturn(CAPTURE);
        when(captureEntryMock.getAmount()).thenReturn(BigDecimal.valueOf(100.0d));
        when(checkoutComMerchantConfigurationServiceMock.getAuthorisationAmountValidationThreshold(SITE_ID)).thenReturn(THRESHOLD);
        when(orderMock.getTotalPrice()).thenReturn(ORDER_TOTAL);
        when(orderMock.getSite().getUid()).thenReturn(SITE_ID);
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
    }

    @Test
    public void isAuthorisedAmountCorrect_WhenAuthorisationAmountWithinThreshold_ShouldReturnTrue() {
        final BigDecimal authorisationAmount = BigDecimal.valueOf(ORDER_TOTAL).subtract(BigDecimal.valueOf(THRESHOLD - 0.01d));
        doReturn(paymentTransaction1Mock).when(testObj).getPaymentTransaction(orderMock);
        when(paymentTransaction1Mock.getEntries()).thenReturn(Arrays.asList(authorisationEntryMock, captureEntryMock));
        when(checkoutComMerchantConfigurationServiceMock.getAuthorisationAmountValidationThreshold(SITE_ID)).thenReturn(THRESHOLD);
        when(authorisationEntryMock.getAmount()).thenReturn(authorisationAmount);

        assertTrue(testObj.isAuthorisedAmountCorrect(orderMock));
    }

    @Test
    public void isAuthorisedAmountCorrect_WhenAuthorisationAmountNotWithinThreshold_ShouldReturnFalse() {
        final BigDecimal authorisationAmount = BigDecimal.valueOf(ORDER_TOTAL).subtract(BigDecimal.valueOf(THRESHOLD + 0.01d));
        doReturn(paymentTransaction1Mock).when(testObj).getPaymentTransaction(orderMock);
        when(paymentTransaction1Mock.getEntries()).thenReturn(Arrays.asList(authorisationEntryMock, captureEntryMock));
        when(checkoutComMerchantConfigurationServiceMock.getAuthorisationAmountValidationThreshold(SITE_ID)).thenReturn(THRESHOLD);
        when(authorisationEntryMock.getAmount()).thenReturn(authorisationAmount);

        assertFalse(testObj.isAuthorisedAmountCorrect(orderMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransaction_WhenPaymentIdIsNull_ShouldThrowException() {
        testObj.createPaymentTransaction(null, orderMock, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransaction_WhenOrderIsNull_ShouldThrowException() {
        testObj.createPaymentTransaction(PAYMENT_ID, null, AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransaction_WhenAmountIsNull_ShouldThrowException() {
        testObj.createPaymentTransaction(PAYMENT_ID, orderMock, null);
    }

    @Test
    public void createPaymentTransaction_WhenEverythingIsCorrectAndCardPayment_ShouldCreatePaymentTransaction() {
        when(orderMock.getCheckoutComPaymentReference()).thenReturn(PAYMENT_REFERENCE);
        when(cardPaymentInfoMock.getCardToken()).thenReturn(REQUEST_TOKEN);

        final PaymentTransactionModel result = testObj.createPaymentTransaction(PAYMENT_ID, orderMock, AMOUNT);

        assertEquals(cardPaymentInfoMock, result.getInfo());
        assertEquals(PAYMENT_REFERENCE, result.getCode());
        assertEquals(currencyModelMock, result.getCurrency());
        assertEquals(orderMock, result.getOrder());
        assertEquals(PAYMENT_ID, result.getRequestId());
        assertEquals(REQUEST_TOKEN, result.getRequestToken());
        assertEquals(AMOUNT, result.getPlannedAmount());
        assertEquals(PROVIDER, result.getPaymentProvider());
    }

    @Test
    public void createPaymentTransaction_WhenEverythingIsCorrectAndApmPayment_ShouldCreatePaymentTransaction() {
        when(orderMock.getCheckoutComPaymentReference()).thenReturn(PAYMENT_REFERENCE);
        when(orderMock.getPaymentInfo()).thenReturn(apmPaymentInfoMock);

        final PaymentTransactionModel result = testObj.createPaymentTransaction(PAYMENT_ID, orderMock, AMOUNT);

        assertEquals(apmPaymentInfoMock, result.getInfo());
        assertEquals(PAYMENT_REFERENCE, result.getCode());
        assertEquals(currencyModelMock, result.getCurrency());
        assertEquals(orderMock, result.getOrder());
        assertEquals(PAYMENT_ID, result.getRequestId());
        assertEquals(AMOUNT, result.getPlannedAmount());
        assertEquals(PROVIDER, result.getPaymentProvider());
        assertNull(result.getRequestToken());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenPaymentTransactionIsNull_ShouldThrowException() {
        testObj.createPaymentTransactionEntry(null, paymentEventMock, ACCEPTED_PAYMENT_STATUS, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenCurrencyIsNull_ShouldThrowException() {
        when(paymentEventMock.getCurrency()).thenReturn(null);

        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED_PAYMENT_STATUS, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenPaymentEventIsNull_ShouldThrowException() {
        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, null, ACCEPTED_PAYMENT_STATUS, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenAuthorizedAmountIsNull_ShouldThrowException() {
        when(paymentEventMock.getAmount()).thenReturn(null);

        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED_PAYMENT_STATUS, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenTransactionStatusIsNull_ShouldThrowException() {
        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, null, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenTransactionStatusDetailsIsNull_ShouldThrowException() {
        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED_PAYMENT_STATUS, null, AUTHORIZATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentTransactionEntry_WhenTransactionTypeIsNull_ShouldThrowException() {
        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED_PAYMENT_STATUS, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, null);
    }

    @Test
    public void createPaymentTransactionEntry_WhenEverythingIsCorrect_ShouldReturnThePaymentTransactionEntry() {
        when(paymentEventMock.getEventType()).thenReturn(PAYMENT_PENDING.getCode());

        testObj.createPaymentTransactionEntry(paymentTransaction1Mock, paymentEventMock, ACCEPTED_PAYMENT_STATUS, SUCCESSFUL_TRANSACTION_STATUS_DETAILS, AUTHORIZATION);

        verify(modelServiceMock).save(paymentTransactionEntryModelCaptor.capture());
        verify(modelServiceMock).refresh(paymentTransaction1Mock);

        final PaymentTransactionEntryModel paymentTransactionEntry = paymentTransactionEntryModelCaptor.getValue();

        assertEquals(PAYMENT_REFERENCE + "-AUTHORIZATION-1", paymentTransactionEntry.getCode());
        assertEquals(AMOUNT, paymentTransactionEntry.getAmount());
        assertEquals(currencyModelMock, paymentTransactionEntry.getCurrency());
        assertEquals(paymentTransaction1Mock, paymentTransactionEntry.getPaymentTransaction());
        assertEquals(PAYMENT_ID, paymentTransactionEntry.getRequestId());
        assertEquals(ACTION_ID, paymentTransactionEntry.getRequestToken());
        assertEquals(ACCEPTED_PAYMENT_STATUS, paymentTransactionEntry.getTransactionStatus());
        assertEquals(SUCCESSFUL_TRANSACTION_STATUS_DETAILS, paymentTransactionEntry.getTransactionStatusDetails());
        assertEquals(CURRENT_DATE, paymentTransactionEntry.getTime());
        assertEquals(AUTHORIZATION, paymentTransactionEntry.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPaymentReferenceFromTransactionEntryCode_WhenPaymentTransactionCodeNull_ShouldThrowException() {
        testObj.getPaymentReferenceFromTransactionEntryCode(null);
    }

    @Test
    public void getPaymentReferenceFromTransactionEntryCode_WhenPaymentTransactionCodeIsPresent_ShouldReturnThePaymentReference() {
        final String result = testObj.getPaymentReferenceFromTransactionEntryCode(PAYMENT_TRANSACTION_ENTRY_CODE);

        assertEquals(PAYMENT_REFERENCE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPaymentTransaction_WhenOrderIsNull_ShouldThrowException() {
        testObj.getPaymentTransaction((OrderModel) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPaymentTransaction_WhenThereAreNotTransactions_ShouldThrowException() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        testObj.getPaymentTransaction(orderMock);
    }

    @Test
    public void getPaymentTransaction_WhenThereIsMoreThanOneTransaction_ShouldReturnTheFirst() {
        when(orderMock.getPaymentTransactions()).thenReturn(asList(paymentTransaction1Mock, paymentTransaction2Mock));

        final PaymentTransactionModel result = testObj.getPaymentTransaction(orderMock);

        assertEquals(PAYMENT_REFERENCE, result.getCode());
        assertEquals(PAYMENT_ID, result.getRequestId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findRefundEntryForActionId_WhenTransactionIsNull_ShouldThrowException() {
        testObj.findRefundEntryForActionId(null, ACTION_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findRefundEntryForActionId_WhenActionIdIsNull_ShouldThrowException() {
        testObj.findRefundEntryForActionId(paymentTransaction1Mock, null);
    }

    @Test
    public void findRefundEntryForActionId_WhenNoRefundEntries_ShouldReturnEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findRefundEntryForActionId(paymentTransaction1Mock, ACTION_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findRefundEntryForActionId_WhenNoRefundEntriesWithActionId_ShouldReturnEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(refundPaymentTransactionEntry1Mock));
        when(refundPaymentTransactionEntry1Mock.getRequestToken()).thenReturn("someActionId");

        final Optional<PaymentTransactionEntryModel> result = testObj.findRefundEntryForActionId(paymentTransaction1Mock, ACTION_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findRefundEntryForActionId_WhenRefundEntriesWithActionId_ShouldReturnCorrectRefundEntry() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(refundPaymentTransactionEntry1Mock, refundPaymentTransactionEntry2Mock));
        when(refundPaymentTransactionEntry1Mock.getRequestToken()).thenReturn("someActionId");
        when(refundPaymentTransactionEntry2Mock.getRequestToken()).thenReturn(ACTION_ID);

        final Optional<PaymentTransactionEntryModel> result = testObj.findRefundEntryForActionId(paymentTransaction1Mock, ACTION_ID);

        assertTrue(result.isPresent());
        assertSame(refundPaymentTransactionEntry2Mock, result.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAcceptedAuthorizationEntry_WhenTransactionIsNull_ShouldThrowException() {
        testObj.findAcceptedAuthorizationEntry(null);
    }

    @Test
    public void findAcceptedAuthorizationEntry_WhenAuthAcceptedTransactionEntryNotFound_ShouldReturnOptionalEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findAcceptedAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findAcceptedAuthorizationEntry_WhenAuthAcceptedTransactionEntryFound_ShouldReturnTheEntry() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, acceptedAuthorizationPaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findAcceptedAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isPresent());
        assertEquals(acceptedAuthorizationPaymentTransactionEntryMock, result.get());
    }

    @Test
    public void findAcceptedAuthorizationEntry_WhenAuthReviewTransactionEntryFound_ShouldReturnTheEntry() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, reviewAuthorizationPaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findAcceptedAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isPresent());
        assertEquals(reviewAuthorizationPaymentTransactionEntryMock, result.get());
    }

    @Test
    public void findPaymentTransaction_WhenOrderDoesNotHaveTransaction_ShouldCreateNewTransaction() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());
        when(paymentEventMock.getPaymentId()).thenReturn(PAYMENT_ID);
        when(paymentEventMock.getAmount()).thenReturn(AMOUNT);
        doReturn(paymentTransaction1Mock).when(testObj).createPaymentTransaction(PAYMENT_ID, orderMock, AMOUNT);

        final PaymentTransactionModel result = testObj.findPaymentTransaction(paymentEventMock, orderMock);

        assertEquals(paymentTransaction1Mock, result);
    }

    @Test
    public void findPaymentTransaction_WhenOrderHasATransaction_ShouldGetTheTransaction() {
        doReturn(paymentTransaction1Mock).when(testObj).getPaymentTransaction(orderMock);

        final PaymentTransactionModel result = testObj.findPaymentTransaction(paymentEventMock, orderMock);

        assertEquals(paymentTransaction1Mock, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findPendingAuthorizationEntry_WhenTransactionIsNull_ShouldThrowException() {
        testObj.findPendingAuthorizationEntry(null);
    }

    @Test
    public void findPendingAuthorizationEntry_WhenTransactionEntryListEmpty_ShouldReturnOptionalEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(emptyList());

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findPendingAuthorizationEntry_WhenAuthTransactionEntryNotFound_ShouldReturnOptionalEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(capturePaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findPendingAuthorizationEntry_WhenAuthTransactionEntryFoundButAccepted_ShouldReturnOptionalEmpty() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(singletonList(acceptedAuthorizationPaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isEmpty());
    }

    @Test
    public void findPendingAuthorizationEntry_WhenAuthPendingTransactionEntryFound_ShouldReturnTheEntry() {
        when(paymentTransaction1Mock.getEntries()).thenReturn(asList(capturePaymentTransactionEntryMock, authorizationPendingPaymentTransactionEntryMock));

        final Optional<PaymentTransactionEntryModel> result = testObj.findPendingAuthorizationEntry(paymentTransaction1Mock);

        assertTrue(result.isPresent());
        assertEquals(authorizationPendingPaymentTransactionEntryMock, result.get());
    }

    private void setUpTestObjMocks() {
        ReflectionTestUtils.setField(testObj, "checkoutComMerchantConfigurationService", checkoutComMerchantConfigurationServiceMock);
        ReflectionTestUtils.setField(testObj, "commercePaymentProviderStrategy", commercePaymentProviderStrategyMock);
        ReflectionTestUtils.setField(testObj, "modelService", modelServiceMock);
        ReflectionTestUtils.setField(testObj, "timeService", timeServiceMock);
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
    }
}