package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.order.process.services.CheckoutComBusinessProcessService;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.*;
import static de.hybris.platform.payment.dto.TransactionStatus.*;
import static de.hybris.platform.payment.dto.TransactionStatusDetails.PROCESSOR_DECLINE;
import static de.hybris.platform.payment.enums.PaymentTransactionType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(JUnitParamsRunner.class)
public class DefaultCheckoutComPaymentEventProcessingServiceTest {

    private static final String WAIT_FOR_EVENT_NAME = "eventName";
    private static final String DECLINED = "20000";
    private static final String APPROVED = "10000";
    private static final String DEFERRED_APPROVED = "10200";
    private static final String ACTION_ID = "actionId";
    private static final String PAYMENT_ID = "paymentId";
    private static final String CARD_TOKEN = "card_token";
    private static final String ORDER_CODE = "order";
    private static final String BUSINESS_PROCESS_CODE = "businessProcessCode";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(123.12d);
    private static final String OTHER_PAYMENT_ID = "OTHER_PAYMENT_ID";
    private static final String EVENT_ID = "event_id";

    @Spy
    @InjectMocks
    private DefaultCheckoutComPaymentEventProcessingService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CheckoutComPaymentService paymentServiceMock;
    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;
    @Mock
    private SessionService sessionServiceMock;
    @Mock
    private CheckoutComBusinessProcessService businessProcessServiceMock;
    @Mock
    private CheckoutComPaymentEventModel eventMock, event2Mock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private OrderModel orderMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private BusinessProcessModel businessProcessMock, otherBusinessProcessMock;
    @Mock
    private PaymentTransactionModel paymentTransactionMock;
    @Mock
    private TransactionStatus transactionStatusMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private PaymentTransactionEntryModel authorizationPendingPaymentTransactionEntryMock;
    @Mock
    private CheckoutComPaymentTransactionService checkoutComPaymentTransactionServiceMock;

    private TransactionOperations transactionOperationsMock = new TransactionOperations() {
        @Override
        public <T> T execute(final TransactionCallback<T> transactionCallback) throws TransactionException {
            return transactionCallback.doInTransaction(transactionStatusMock);
        }
    };

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(testObj.getTransactionTemplate()).thenReturn(transactionOperationsMock);

        when(eventMock.getAmount()).thenReturn(AMOUNT);
        when(eventMock.getCurrency()).thenReturn(currencyModelMock);
        when(eventMock.getPaymentId()).thenReturn(PAYMENT_ID);
        when(eventMock.getActionId()).thenReturn(ACTION_ID);
        when(eventMock.getPaymentId()).thenReturn(PAYMENT_ID);
        when(eventMock.getEventId()).thenReturn(EVENT_ID);

        when(orderMock.getPaymentInfo()).thenReturn(paymentInfoMock);
        when(orderMock.getCode()).thenReturn(ORDER_CODE);

        when(businessProcessMock.getCode()).thenReturn(BUSINESS_PROCESS_CODE);
        when(paymentInfoMock.getCardToken()).thenReturn(CARD_TOKEN);
        when(paymentInfoMock.getPaymentId()).thenReturn(PAYMENT_ID);

        when(sessionServiceMock.executeInLocalView(any(SessionExecutionBody.class))).thenAnswer(invocation -> {
            final SessionExecutionBody args = (SessionExecutionBody) invocation.getArguments()[0];
            return args.execute();
        });

        when(authorizationPendingPaymentTransactionEntryMock.getType()).thenReturn(AUTHORIZATION);
        when(authorizationPendingPaymentTransactionEntryMock.getTransactionStatus()).thenReturn(PENDING.toString());
        when(checkoutComPaymentTransactionServiceMock.findPaymentTransaction(eventMock, orderMock)).thenReturn(paymentTransactionMock);
        when(businessProcessServiceMock.findBusinessProcess(CAPTURE, orderMock, eventMock)).thenReturn(singletonList(businessProcessMock));
        when(paymentInfoServiceMock.findAbstractOrderByPaymentId(PAYMENT_ID)).thenReturn(ImmutableList.of(orderMock));
        when(businessProcessServiceMock.findBusinessProcess(CANCEL, orderMock, eventMock)).thenReturn(singletonList(businessProcessMock));
        when(businessProcessServiceMock.findBusinessProcess(AUTHORIZATION, orderMock, eventMock)).thenReturn(singletonList(businessProcessMock));
        when(paymentServiceMock.isVoidPending(orderMock)).thenReturn(false);
    }

    @Test
    public void processPaymentEvents_WhenThereAreNotEvents_ShouldDoNothing() {
        testObj.processPaymentEvents(emptyList(), AUTHORIZATION);

        verify(testObj, never()).handleEvents(anyList(), any(PaymentTransactionType.class));
    }

    @Test
    public void processPaymentEvents_WhenThereAreEvents_ShouldProcessEvents() {
        final List<CheckoutComPaymentEventModel> paymentEvents = singletonList(eventMock);

        doNothing().when(testObj).handleEvents(anyList(), any(PaymentTransactionType.class));
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));

        testObj.processPaymentEvents(paymentEvents, AUTHORIZATION);

        verify(testObj).handleEvents(paymentEvents, AUTHORIZATION);
    }

    @Test
    public void handleEvents_WhenThereIsNoOrderForEvent_ShouldFailAndSetFailureReason() {
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));

        when(paymentInfoServiceMock.findAbstractOrderByPaymentId(PAYMENT_ID)).thenReturn(emptyList());

        testObj.handleEvents(singletonList(eventMock), AUTHORIZATION);

        verify(eventMock).setFailReason(anyString());
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.FAILED);
    }

    @Test
    public void handleEvents_WhenThereIsOnlyCartForEvent_ShouldDoNothing() {
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));

        when(paymentInfoServiceMock.findAbstractOrderByPaymentId(PAYMENT_ID)).thenReturn(ImmutableList.of(cartModelMock));

        testObj.handleEvents(singletonList(eventMock), AUTHORIZATION);

        verify(eventMock, never()).setFailReason(anyString());
        verify(testObj, never()).updateEventStatus(eq(singletonList(eventMock)), any());
    }

    @Test
    public void handleEvents_WhenThereIsAnException_ShouldFailAndSetFailureReason() {
        doThrow(new RuntimeException("exception")).when(testObj).processEventInTransaction(eq(AUTHORIZATION), anyList(), anyList(), anyList(), eq(eventMock));

        testObj.handleEvents(singletonList(eventMock), AUTHORIZATION);

        verify(eventMock).setFailReason(anyString());
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.FAILED);
    }

    @Test
    public void handleEventProcess_WhenThereIsAnOrderWithoutBusinessProcess_ShouldSkipProcessing() {
        when(businessProcessServiceMock.findBusinessProcess(CAPTURE, orderMock, eventMock)).thenReturn(emptyList());
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(testObj, never()).processEvent(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionType.class), any(OrderModel.class), any(BusinessProcessModel.class));
        verify(testObj, never()).updateEventStatus(eq(singletonList(eventMock)), any());
    }

    @Test
    public void handleEventProcess_WhenThereIsAnOrderWithSingleBusinessProcessButWaitForPaymentTransaction_ShouldSkipProcessing() {
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));
        doReturn(true).when(testObj).shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(testObj, never()).processEvent(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionType.class), any(OrderModel.class), any(BusinessProcessModel.class));
        verify(testObj, never()).updateEventStatus(eq(singletonList(eventMock)), any());
    }

    @Test
    public void handleEventProcess_WhenThereIsAnOrderWithMoreThanOneBusinessProcess_ShouldSkipProcessingAndPutEventInFailed() {
        when(businessProcessServiceMock.findBusinessProcess(CAPTURE, orderMock, eventMock)).thenReturn(asList(businessProcessMock, otherBusinessProcessMock));
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        when(eventMock.getEventType()).thenReturn(CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(eventMock).setFailReason(anyString());
        verify(testObj, never()).processEvent(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionType.class), any(OrderModel.class), any(BusinessProcessModel.class));
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.FAILED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.PENDING);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.COMPLETED);
    }

    @Test
    public void handleEventProcess_WhenOrderPaymentInfoIsNull_ShouldPutEventInFailed() {
        when(paymentInfoServiceMock.findAbstractOrderByPaymentId(PAYMENT_ID)).thenReturn(emptyList());

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(eventMock).setFailReason(anyString());
        verify(testObj, never()).processEvent(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionType.class), any(OrderModel.class), any(BusinessProcessModel.class));
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.FAILED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.PENDING);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.COMPLETED);
    }

    @Test
    public void handleEventProcess_WhenThereIsAnOrderWithSingleBusinessProcessAndPaymentIdMatches_ShouldProcessEventAndPutEventInCompleted() {
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));
        when(eventMock.getEventType()).thenReturn(CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doReturn(true).when(testObj).processEvent(eventMock, CAPTURE, orderMock, businessProcessMock);

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(eventMock, never()).setFailReason(anyString());
        verify(testObj, never()).filterEquivalentAuthorisationEvents(anyList(), anyList());
        verify(testObj).processEvent(eventMock, CAPTURE, orderMock, businessProcessMock);
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.COMPLETED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.FAILED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.PENDING);
    }

    @Test
    public void handleEventProcess_WhenThereIsAnOrderWithSingleBusinessProcessButEventIgnored_ShouldPutEventInIgnored() {
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));
        when(eventMock.getEventType()).thenReturn(CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doReturn(false).when(testObj).processEvent(eventMock, CAPTURE, orderMock, businessProcessMock);

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(eventMock, never()).setFailReason(anyString());
        verify(testObj).processEvent(eventMock, CAPTURE, orderMock, businessProcessMock);
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.IGNORED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.FAILED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.PENDING);
    }

    @Test
    public void handleEventProcess_WhenThereIsAnOrderWithSingleBusinessProcessButPaymentIdDoesNotMatch_ShouldSkipProcessEventAndPutEventInIgnored() {
        when(paymentInfoServiceMock.findAbstractOrderByPaymentId(OTHER_PAYMENT_ID)).thenReturn(ImmutableList.of(orderMock));
        when(eventMock.getEventType()).thenReturn(CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doNothing().when(testObj).updateEventStatus(anyList(), any(CheckoutComPaymentEventStatus.class));
        when(eventMock.getPaymentId()).thenReturn(OTHER_PAYMENT_ID);

        testObj.handleEvents(singletonList(eventMock), CAPTURE);

        verify(eventMock).setFailReason(anyString());
        verify(testObj, never()).processEvent(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionType.class), any(OrderModel.class), any(BusinessProcessModel.class));
        verify(testObj).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.IGNORED);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.PENDING);
        verify(testObj, never()).updateEventStatus(singletonList(eventMock), CheckoutComPaymentEventStatus.COMPLETED);
    }

    @Test
    public void handleEvents_WhenCancelTransactionTypeAndVoidPendingTransactionEntry_ShouldProcessEvent() {
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CANCEL, PAYMENT_CANCELED.getCode());
        when(eventMock.getEventType()).thenReturn(PAYMENT_CANCELED.getCode());
        when(paymentServiceMock.isVoidPending(orderMock)).thenReturn(true);

        testObj.handleEvents(singletonList(eventMock), CANCEL);

        verify(testObj).processEvent(eventMock, CANCEL, orderMock, businessProcessMock);
    }

    @Test
    public void handleEvents_WhenCancelTransactionTypeAndNoVoidPendingTransactionEntry_ShouldNotProcessEvent() {
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, CANCEL, PAYMENT_CANCELED.getCode());
        when(eventMock.getEventType()).thenReturn(PAYMENT_CANCELED.getCode());

        testObj.handleEvents(singletonList(eventMock), CANCEL);

        verify(testObj, never()).processEvent(eventMock, CANCEL, orderMock, businessProcessMock);
    }

    @Test
    public void handleEvents_WhenAuthoriseTransactionTypeAndNoVoidPendingTransactionEntry_ShouldProcessEvent() {
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, PAYMENT_CANCELED.getCode());
        when(eventMock.getEventType()).thenReturn(PAYMENT_CANCELED.getCode());

        testObj.handleEvents(singletonList(eventMock), AUTHORIZATION);

        verify(testObj).processEvent(eventMock, AUTHORIZATION, orderMock, businessProcessMock);
    }

    @Test
    public void handleEvents_WhenAuthoriseTransactionTypeAndVoidPendingTransactionEntry_ShouldNotProcessEvent() {
        doReturn(false).when(testObj).shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, PAYMENT_CANCELED.getCode());
        when(eventMock.getEventType()).thenReturn(PAYMENT_CANCELED.getCode());
        when(paymentServiceMock.isVoidPending(orderMock)).thenReturn(true);

        testObj.handleEvents(singletonList(eventMock), AUTHORIZATION);

        verify(testObj, never()).processEvent(eventMock, AUTHORIZATION, orderMock, businessProcessMock);
    }

    @Test
    public void processEvent_WhenAuthAlreadyExistsAndEventTypeIsAuthorise_ShouldSetTheFailReasonAndReturnFalse() {
        when(checkoutComPaymentTransactionServiceMock.findAcceptedAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.empty());
        when(checkoutComPaymentTransactionServiceMock.findPendingAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.of(authorizationPendingPaymentTransactionEntryMock));
        when(eventMock.getEventType()).thenReturn(PAYMENT_APPROVED.getCode());
        doReturn(WAIT_FOR_EVENT_NAME).when(testObj).createWaitForEventName(eventMock, AUTHORIZATION, businessProcessMock);
        doNothing().when(testObj).processPayment(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionType.class));
        doNothing().when(testObj).updateDeferredAuthorization(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionType.class), any(BusinessProcessModel.class));

        final boolean result = testObj.processEvent(eventMock, AUTHORIZATION, orderMock, businessProcessMock);

        assertThat(result).isFalse();
        ;
        verify(testObj, never()).filterEquivalentAuthorisationEvents(anyList(), anyList());
        verify(testObj, never()).processPayment(eventMock, paymentTransactionMock, AUTHORIZATION);
        verifyZeroInteractions(businessProcessServiceMock);
    }

    @Test
    public void processEvent_WhenEventTypeIsCaptureAndPaymentIsDeferred_ShouldProcessThePaymentUpdateDeferredTransactionTriggerTheEventAndReturnTrue() {
        when(paymentServiceMock.isDeferred(orderMock)).thenReturn(true);
        when(checkoutComPaymentTransactionServiceMock.findAcceptedAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.empty());
        when(checkoutComPaymentTransactionServiceMock.findPendingAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.of(authorizationPendingPaymentTransactionEntryMock));
        when(eventMock.getEventType()).thenReturn(CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());
        doReturn(WAIT_FOR_EVENT_NAME).when(testObj).createWaitForEventName(eventMock, CAPTURE, businessProcessMock);
        doNothing().when(testObj).processPayment(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionType.class));
        doNothing().when(testObj).updateDeferredAuthorization(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionType.class), any(BusinessProcessModel.class));

        final boolean result = testObj.processEvent(eventMock, CAPTURE, orderMock, businessProcessMock);

        assertThat(result).isTrue();
        ;
        InOrder inOrder = inOrder(testObj, businessProcessServiceMock);
        inOrder.verify(testObj).processPayment(eventMock, paymentTransactionMock, CAPTURE);
        inOrder.verify(testObj).updateDeferredAuthorization(eventMock, paymentTransactionMock, CAPTURE, businessProcessMock);
        inOrder.verify(businessProcessServiceMock).triggerEvent(WAIT_FOR_EVENT_NAME);
    }

    @Test
    public void processEvent_WhenEventTypeIsVoid_ShouldProcessThePaymentTriggerTheEventAndReturnTrue() {
        when(paymentServiceMock.isDeferred(orderMock)).thenReturn(false);
        when(checkoutComPaymentTransactionServiceMock.findAcceptedAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.empty());
        when(checkoutComPaymentTransactionServiceMock.findPendingAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.of(authorizationPendingPaymentTransactionEntryMock));
        when(eventMock.getEventType()).thenReturn(CheckoutComPaymentEventType.PAYMENT_VOIDED.getCode());
        doReturn(WAIT_FOR_EVENT_NAME).when(testObj).createWaitForEventName(eventMock, CANCEL, businessProcessMock);
        doNothing().when(testObj).processPayment(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionType.class));
        doNothing().when(testObj).updateDeferredAuthorization(any(CheckoutComPaymentEventModel.class), any(PaymentTransactionModel.class), any(PaymentTransactionType.class), any(BusinessProcessModel.class));

        final boolean result = testObj.processEvent(eventMock, CANCEL, orderMock, businessProcessMock);

        assertThat(result).isTrue();
        InOrder inOrder = inOrder(testObj, businessProcessServiceMock);
        inOrder.verify(testObj).processPayment(eventMock, paymentTransactionMock, CANCEL);
        inOrder.verify(testObj, never()).updateDeferredAuthorization(eventMock, paymentTransactionMock, CANCEL, businessProcessMock);
        inOrder.verify(businessProcessServiceMock).triggerEvent(WAIT_FOR_EVENT_NAME);
    }

    @Test
    @Parameters({"PAYMENT_APPROVED","PAYMENT_CAPTURE_PENDING"})
    public void equivalentEventAlreadyFound_WhenEquivalentAuthorisationForTheSamePaymentId_ShouldAddPendingAuthorisationAsIgnored(final String eventCode) {
        final List<CheckoutComPaymentEventModel> ignoredEvents = new ArrayList<>();
        final List<CheckoutComPaymentEventModel> eventsToProcess = new ArrayList<>(asList(eventMock, event2Mock));
        when(eventMock.getEventType()).thenReturn(eventCode);
        when(event2Mock.getPaymentId()).thenReturn(PAYMENT_ID);
        when(event2Mock.getEventType()).thenReturn(PAYMENT_PENDING.getCode());

        testObj.filterEquivalentAuthorisationEvents(eventsToProcess, ignoredEvents);

        assertThat(eventsToProcess).containsExactly(eventMock);
        assertThat(ignoredEvents).containsExactly(event2Mock);
    }

    @Test
    @Parameters({"PAYMENT_APPROVED","PAYMENT_CAPTURE_PENDING"})
    public void equivalentEventAlreadyFound_WhenNoEquivalentAuthorisationForTheSamePaymentId_ShouldDoNothing(final String eventCode) {
        final List<CheckoutComPaymentEventModel> ignoredEvents = new ArrayList<>();
        final List<CheckoutComPaymentEventModel> eventsToProcess = new ArrayList<>(asList(eventMock, event2Mock));
        when(eventMock.getEventType()).thenReturn(eventCode);
        when(event2Mock.getPaymentId()).thenReturn(PAYMENT_ID);
        when(event2Mock.getEventType()).thenReturn(PAYMENT_CAPTURED.getCode());

        testObj.filterEquivalentAuthorisationEvents(eventsToProcess, ignoredEvents);

        assertThat(ignoredEvents).isEmpty();
        assertThat(eventsToProcess).containsExactlyInAnyOrder(eventMock, event2Mock);
    }

    @Test
    public void handleEventProcess_WhenAuthorisationType_ShouldFilterAuthorisationEvents() {
        testObj.handleEvents(asList(event2Mock, eventMock), AUTHORIZATION);

        verify(testObj).filterEquivalentAuthorisationEvents(asList(event2Mock, eventMock), emptyList());
    }

    @Test
    public void processPayment_WhenEventDeclined_ShouldRejectedPayment() {
        when(eventMock.getResponseCode()).thenReturn(DECLINED);

        testObj.processPayment(eventMock, paymentTransactionMock, AUTHORIZATION);

        verify(paymentServiceMock).rejectPayment(eventMock, paymentTransactionMock, AUTHORIZATION);
    }

    @Test
    public void processPayment_WhenEventIsApproved_ShouldAcceptPayment() {
        when(eventMock.getResponseCode()).thenReturn(APPROVED);

        testObj.processPayment(eventMock, paymentTransactionMock, AUTHORIZATION);

        verify(paymentServiceMock).acceptPayment(eventMock, paymentTransactionMock, AUTHORIZATION);
    }

    @Test
    public void processPayment_WhenEventResponseIsNotRefund_andIsDeferredApproved_ShouldRejectPayment() {
        when(eventMock.getResponseCode()).thenReturn(DEFERRED_APPROVED);

        testObj.processPayment(eventMock, paymentTransactionMock, AUTHORIZATION);

        verify(paymentServiceMock).rejectPayment(eventMock, paymentTransactionMock, AUTHORIZATION);
    }

    @Test
    public void processPayment_WhenEventResponseIsRefund_andDeferredApproved_ShouldAcceptPayment() {
        when(eventMock.getResponseCode()).thenReturn(DEFERRED_APPROVED);

        testObj.processPayment(eventMock, paymentTransactionMock, REFUND_FOLLOW_ON);

        verify(paymentServiceMock).acceptPayment(eventMock, paymentTransactionMock, REFUND_FOLLOW_ON);
    }

    @Test
    public void processPayment_WhenEventResponseIsReturn_ShouldReturnPayment() {
        testObj.processPayment(eventMock, paymentTransactionMock, RETURN);

        verify(paymentServiceMock).returnPayment(eventMock, paymentTransactionMock, RETURN);
    }

    @Test
    public void createWaitForEventName_WhenRefundPayment_ShouldConcatenateProcessCodeActionIdAndPaymentTransactionType() {
        when(businessProcessMock.getCode()).thenReturn(BUSINESS_PROCESS_CODE);

        final String result = testObj.createWaitForEventName(eventMock, REFUND_FOLLOW_ON, businessProcessMock);

        assertThat(BUSINESS_PROCESS_CODE + "_" + ACTION_ID + "_" + REFUND_FOLLOW_ON).isEqualTo(result);
    }

    @Test
    public void createWaitForEventName_WhenOtherPayment_ShouldConcatenateProcessCodeAndPaymentTransactionType() {
        when(businessProcessMock.getCode()).thenReturn(BUSINESS_PROCESS_CODE);

        final String result = testObj.createWaitForEventName(eventMock, CAPTURE, businessProcessMock);

        assertThat(BUSINESS_PROCESS_CODE + "_" + CAPTURE).isEqualTo(result);
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeAuthorizationAndEventTypeIsPaymentApproved_ShouldReturnFalse() {
        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, PAYMENT_APPROVED.getCode());

        assertThat(result).isFalse();
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeAuthorizationAndEventTypeIsPaymentPending_ShouldReturnFalse() {
        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, CheckoutComPaymentEventType.PAYMENT_PENDING.getCode());

        assertThat(result).isFalse();
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeAuthorizationButEventTypePaymentDeclinedAndNoPaymentTransactionsInOrder_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, PAYMENT_DECLINED.getCode());

        assertThat(result).isTrue();
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeAuthorizationButEventTypePaymentCanceledAndNoPaymentTransactionsInOrder_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, PAYMENT_CANCELED.getCode());

        assertThat(result).isTrue();
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeAuthorizationButEventTypePaymentExpiredAndNoPaymentTransactionsInOrder_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, AUTHORIZATION, CheckoutComPaymentEventType.PAYMENT_EXPIRED.getCode());

        assertThat(result).isTrue();
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeNonAuthorizationAndNoPaymentTransactionsInOrder_ShouldReturnTrue() {
        when(orderMock.getPaymentTransactions()).thenReturn(emptyList());

        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());

        assertThat(result).isTrue();
    }

    @Test
    public void shouldWaitForPaymentTransaction_WhenTransactionTypeNonAuthorizationAndPaymentTransactionInOrder_ShouldReturnFalse() {
        when(orderMock.getPaymentTransactions()).thenReturn(singletonList(paymentTransactionMock));

        final boolean result = testObj.shouldWaitForPaymentTransaction(orderMock, CAPTURE, CheckoutComPaymentEventType.PAYMENT_CAPTURED.getCode());

        assertThat(result).isFalse();
    }

    @Test
    public void updateEventStatus_WhenPaymentEventListIsEmpty_ShouldDoNothing() {
        testObj.updateEventStatus(Collections.EMPTY_LIST, CheckoutComPaymentEventStatus.PENDING);

        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void updateEventStatus_WhenPaymentEventListNotEmpty_ShouldUpdateAndSaveStatusForEachEvent() {
        final List<CheckoutComPaymentEventModel> paymentEvents = asList(eventMock, event2Mock);

        testObj.updateEventStatus(paymentEvents, CheckoutComPaymentEventStatus.PENDING);

        verify(eventMock).setStatus(CheckoutComPaymentEventStatus.PENDING);
        verify(event2Mock).setStatus(CheckoutComPaymentEventStatus.PENDING);
        verify(modelServiceMock).saveAll(paymentEvents);
    }

    @Test
    public void updateDeferredAuthorization_WhenPendingAuthorizationEntryDoesNotExists_ShouldDoNothing() {
        when(checkoutComPaymentTransactionServiceMock.findPendingAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.empty());

        testObj.updateDeferredAuthorization(eventMock, paymentTransactionMock, CAPTURE, businessProcessMock);

        verifyZeroInteractions(modelServiceMock);
        verifyZeroInteractions(businessProcessServiceMock);
    }

    @Test
    public void updateDeferredAuthorization_WhenPendingAuthorizationEntryAndCaptureEventType_ShouldUpdateTransactionEntryAsAcceptedAndTriggerTheEvent() {
        when(checkoutComPaymentTransactionServiceMock.findPendingAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.of(authorizationPendingPaymentTransactionEntryMock));

        testObj.updateDeferredAuthorization(eventMock, paymentTransactionMock, CAPTURE, businessProcessMock);

        verify(authorizationPendingPaymentTransactionEntryMock).setTransactionStatus(ACCEPTED.name());
        verify(modelServiceMock).save(authorizationPendingPaymentTransactionEntryMock);
        verify(businessProcessServiceMock).triggerEvent(anyString());
    }

    @Test
    public void updateDeferredAuthorization_WhenPendingAuthorizationEntryAndPaymentDeclinedEventType_ShouldUpdateTransactionEntryAsRejectedAndTriggerTheEvent() {
        when(checkoutComPaymentTransactionServiceMock.findPendingAuthorizationEntry(paymentTransactionMock)).thenReturn(Optional.of(authorizationPendingPaymentTransactionEntryMock));

        testObj.updateDeferredAuthorization(eventMock, paymentTransactionMock, AUTHORIZATION, businessProcessMock);

        verify(authorizationPendingPaymentTransactionEntryMock).setTransactionStatus(REJECTED.name());
        verify(authorizationPendingPaymentTransactionEntryMock).setTransactionStatusDetails(PROCESSOR_DECLINE.name());
        verify(modelServiceMock).save(authorizationPendingPaymentTransactionEntryMock);
        verify(businessProcessServiceMock).triggerEvent(anyString());
    }
}
