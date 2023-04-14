package com.checkout.hybris.events.services.impl;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventStatus;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.hybris.events.order.process.services.CheckoutComBusinessProcessService;
import com.checkout.hybris.events.services.CheckoutComPaymentEventProcessingService;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.checkout.hybris.events.constants.CheckouteventsConstants.EVENT_APPROVED_RESPONSE_CODE;
import static com.checkout.hybris.events.constants.CheckouteventsConstants.DEFERRED_EVENT_APPROVED_RESPONSE_CODE;
import static com.checkout.hybris.events.enums.CheckoutComPaymentEventType.*;
import static de.hybris.platform.payment.dto.TransactionStatus.ACCEPTED;
import static de.hybris.platform.payment.dto.TransactionStatus.REJECTED;
import static de.hybris.platform.payment.dto.TransactionStatusDetails.PROCESSOR_DECLINE;
import static de.hybris.platform.payment.enums.PaymentTransactionType.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;

/**
 * Default implementation of {@link CheckoutComPaymentEventProcessingService}
 */
public class DefaultCheckoutComPaymentEventProcessingService implements CheckoutComPaymentEventProcessingService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentEventProcessingService.class);

    protected static final String SEPARATOR = "_";

    protected final ModelService modelService;
    protected final CheckoutComPaymentService paymentService;
    protected final CheckoutComPaymentInfoService paymentInfoService;
    protected final SessionService sessionService;
    protected final CheckoutComBusinessProcessService businessProcessService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;
    protected final TransactionOperations transactionTemplate;

    public DefaultCheckoutComPaymentEventProcessingService(final ModelService modelService,
                                                           final CheckoutComPaymentService paymentService,
                                                           final CheckoutComPaymentInfoService paymentInfoService,
                                                           final SessionService sessionService,
                                                           final CheckoutComBusinessProcessService businessProcessService,
                                                           final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService,
                                                           final TransactionOperations transactionTemplate) {
        this.modelService = modelService;
        this.paymentService = paymentService;
        this.paymentInfoService = paymentInfoService;
        this.sessionService = sessionService;
        this.businessProcessService = businessProcessService;
        this.checkoutComPaymentTransactionService = checkoutComPaymentTransactionService;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPaymentEvents(final List<CheckoutComPaymentEventModel> paymentEvents, final PaymentTransactionType transactionType) {
        if (CollectionUtils.isEmpty(paymentEvents)) {
            LOG.warn("No payment events found to process for transaction type [{}].", transactionType.getCode());
            return;
        }
        handleEvents(paymentEvents, transactionType);
    }

    /**
     * Handles events processing and completion
     *
     * @param paymentEvents   the events to handle
     * @param transactionType the transaction type for the event
     */
    protected void handleEvents(final List<CheckoutComPaymentEventModel> paymentEvents, final PaymentTransactionType transactionType) {
        final List<CheckoutComPaymentEventModel> completedEvents = new ArrayList<>();
        final List<CheckoutComPaymentEventModel> failedEvents = new ArrayList<>();
        final List<CheckoutComPaymentEventModel> ignoredEvents = new ArrayList<>();
        final List<CheckoutComPaymentEventModel> eventsToProcess = new ArrayList<>(paymentEvents);

        if (AUTHORIZATION.equals(transactionType)) {
            filterEquivalentAuthorisationEvents(eventsToProcess, ignoredEvents);
        }
        eventsToProcess.forEach(event -> {
            try {
                getTransactionTemplate().execute(transactionStatus -> {
                    processEventInTransaction(transactionType, completedEvents, failedEvents, ignoredEvents, event);
                    return null;
                });
            } catch (final Exception e) {
                LOG.error("Exception processing the event with id [{}].", event.getEventId());
                event.setFailReason(String.format("Exception while processing event [%s]. Exception: [%s]", event.getEventId(), ExceptionUtils.getStackTrace(e)));
                updateEventStatus(singletonList(event), CheckoutComPaymentEventStatus.FAILED);
            }
        });
    }

    /**
     * Handles equivalent authorisation events for the same order. One of the events will be added to the ignore list and removed from the events to be processed list,
     * the other one will get processed as normally
     *
     * @param eventsToProcess list of events to be processed
     * @param ignoredEvents   list of events to be ignored
     */
    protected void filterEquivalentAuthorisationEvents(final List<CheckoutComPaymentEventModel> eventsToProcess, final List<CheckoutComPaymentEventModel> ignoredEvents) {
        eventsToProcess.stream()
                .filter(this::isAuthoriseEventType)
                .collect(groupingBy(CheckoutComPaymentEventModel::getPaymentId))
                .forEach((paymentId, eventList) -> {
                    final Optional<CheckoutComPaymentEventModel> pendingEvent = findEventInList(eventList, PAYMENT_PENDING);
                    final Optional<CheckoutComPaymentEventModel> approvedEvent = findEventInList(eventList, PAYMENT_APPROVED);
                    final Optional<CheckoutComPaymentEventModel> capturePendingEvent = findEventInList(eventList, PAYMENT_CAPTURE_PENDING);

                    if (eventList.size() > 1 && (pendingEvent.isPresent() && approvedEvent.isPresent()) || (pendingEvent.isPresent() && capturePendingEvent.isPresent())) {
                        updateFailReasonAndAddEventToList(String.format("Equivalent event was already found for the paymentId: [%s] .", paymentId), pendingEvent.get(), ignoredEvents);
                        eventsToProcess.remove(pendingEvent.get());
                    }
                });
    }

    /**
     * Processes the single event
     *
     * @param transactionType the transaction type of the event
     * @param completedEvents the completed events list
     * @param failedEvents    the failed events list
     * @param ignoredEvents   the ignored events list
     * @param event           the event being processed
     */
    protected void processEventInTransaction(final PaymentTransactionType transactionType,
                                             final List<CheckoutComPaymentEventModel> completedEvents,
                                             final List<CheckoutComPaymentEventModel> failedEvents,
                                             final List<CheckoutComPaymentEventModel> ignoredEvents,
                                             final CheckoutComPaymentEventModel event) {

        final List<AbstractOrderModel> abstractOrders = paymentInfoService.findAbstractOrderByPaymentId(event.getPaymentId());
        final Optional<AbstractOrderModel> optionalOrder = abstractOrders.stream().filter(OrderModel.class::isInstance).findAny();

        if (optionalOrder.isPresent()) {
            final OrderModel order = (OrderModel) optionalOrder.get();

            final List<BusinessProcessModel> businessProcess = businessProcessService.findBusinessProcess(transactionType, order, event);
            if (businessProcess.isEmpty() || shouldWaitForPaymentTransaction(order, transactionType, event.getEventType())) {
                LOG.warn("Could not find business process or payment transaction for event [{}] [{}]. Skipping event processing", event.getEventId(), transactionType);
            } else if (businessProcess.size() > 1) {
                updateFailReasonAndAddEventToList(String.format("Found [%s] business processes for order [%s] and transactionType [%s]. Must be one.",
                        businessProcess.size(), order.getCode(), transactionType), event, failedEvents);
            } else if (!failedEvents.contains(event) && isEventPaymentIdValidForPaymentInfo(failedEvents, ignoredEvents, event, order)) {
                //checks if transaction should be processed in this cronjob or not
                if (shouldNotProcessEvent(transactionType, order)) {
                    LOG.info("Event [{}] [{}] will not be processed in this cronjob. Skipping event processing", event.getEventId(), transactionType);
                } else {
                    processEventsAndAddToCorrectList(transactionType, completedEvents, ignoredEvents, event, order, businessProcess);
                }
            }
        } else {
            handleEventsRelatedToCart(abstractOrders, event, failedEvents);
        }

        updateEventStatus(completedEvents, CheckoutComPaymentEventStatus.COMPLETED);
        updateEventStatus(failedEvents, CheckoutComPaymentEventStatus.FAILED);
        updateEventStatus(ignoredEvents, CheckoutComPaymentEventStatus.IGNORED);
    }

    /**
     * Returns true if the event should be processed on the current cronJob, false otherwise
     * This is done to handle the cases when cronJobs compete to process the event payment_pending
     * If there is a pending transaction and cronJob is Cancel, the event should be processed, otherwise authorization cronJob will process it
     *
     * @param transactionType the transaction type of the event
     * @param order           the order linked to the event
     * @return true if the event should be processed, false otherwise
     */
    protected boolean shouldNotProcessEvent(final PaymentTransactionType transactionType, final OrderModel order) {
        boolean isVoidPending = paymentService.isVoidPending(order);
        return (CANCEL.equals(transactionType) && !isVoidPending) || (AUTHORIZATION.equals(transactionType) && isVoidPending);
    }

    /**
     * Decides whether to wait for the transaction to exists in case it should exist based on the transaction type and
     * event type.
     *
     * @param order           the order
     * @param transactionType the transaction type
     * @param eventType       the event type
     * @return true if the wait is required, false otherwise
     */
    protected boolean shouldWaitForPaymentTransaction(final OrderModel order, final PaymentTransactionType transactionType, final String eventType) {
        return (!PaymentTransactionType.AUTHORIZATION.equals(transactionType) ||
                isAuthorizationDeclinedEvent(eventType)) &&
                CollectionUtils.isEmpty(order.getPaymentTransactions());
    }

    /**
     * Processes a single event by creating a payment transaction, transaction entry and triggering the business process waitFor event.
     *
     * @param event           the event being processed
     * @param transactionType the transaction type of the event
     * @param order           the order related to the event
     * @param businessProcess the business process related to the event
     * @return true if the event has been processed, false otherwise
     */
    protected boolean processEvent(final CheckoutComPaymentEventModel event,
                                   final PaymentTransactionType transactionType,
                                   final OrderModel order,
                                   final BusinessProcessModel businessProcess) {

        return sessionService.executeInLocalView(new SessionExecutionBody() {
            @Override
            public Boolean execute() {
                sessionService.setAttribute("currentSite", order.getSite());

                final PaymentTransactionModel paymentTransaction = checkoutComPaymentTransactionService.findPaymentTransaction(event, order);

                if (doesAuthorisationAlreadyExist(paymentTransaction) &&
                        isAuthoriseEventType(event)) {
                    LOG.error("An authorization payment entry already exists for the order [{}] ", order.getCode());
                    event.setFailReason(String.format("An authorization payment entry already exists for the order [%s] ", order.getCode()));
                    return false;
                }

                processPayment(event, paymentTransaction, transactionType);

                if (CAPTURE.equals(transactionType) || isAuthorizationDeclinedEvent(event.getEventType()) && paymentService.isDeferred(order)) {
                    updateDeferredAuthorization(event, paymentTransaction, transactionType, businessProcess);
                }

                businessProcessService.triggerEvent(createWaitForEventName(event, transactionType, businessProcess));

                return true;
            }
        });
    }

    /**
     * Accepts or rejects the payment based on the event response code
     *
     * @param event           the payment event
     * @param transaction     the payment transaction
     * @param transactionType the transaction type
     */
    protected void processPayment(final CheckoutComPaymentEventModel event, final PaymentTransactionModel transaction, final PaymentTransactionType transactionType) {
        if (RETURN.equals(transactionType)) {
            paymentService.returnPayment(event, transaction, transactionType);
        }
        else if (EVENT_APPROVED_RESPONSE_CODE.equalsIgnoreCase(event.getResponseCode()) || isDeferredRefund(event, transactionType)) {
            LOG.debug("Accepting payment of type [{}] based on event with id [{}]", transactionType.toString(), event.getEventId());
            paymentService.acceptPayment(event, transaction, transactionType);
        }
         else {
            LOG.debug("Rejecting payment of transaction type [{}] based on event with id [{}]", transactionType.toString(), event.getEventId());
            paymentService.rejectPayment(event, transaction, transactionType);
        }
    }

    /**
     * Updates the deferred authorization transaction entry to ACCEPTED if it's a CAPTURE event, to REJECTED otherwise,
     * and triggers the event to unblock the the order process
     *
     * @param event           the payment event
     * @param transaction     the payment transaction
     * @param transactionType the transaction type of the event
     * @param businessProcess the order process
     */
    protected void updateDeferredAuthorization(final CheckoutComPaymentEventModel event,
                                               final PaymentTransactionModel transaction,
                                               final PaymentTransactionType transactionType,
                                               final BusinessProcessModel businessProcess) {

        final Optional<PaymentTransactionEntryModel> pendingAuthorizationEntry = checkoutComPaymentTransactionService.findPendingAuthorizationEntry(transaction);
        if (pendingAuthorizationEntry.isPresent()) {
            if (CAPTURE.equals(transactionType)) {
                pendingAuthorizationEntry.get().setTransactionStatus(ACCEPTED.name());
            } else {
                pendingAuthorizationEntry.get().setTransactionStatus(REJECTED.name());
                pendingAuthorizationEntry.get().setTransactionStatusDetails(PROCESSOR_DECLINE.name());
            }
            modelService.save(pendingAuthorizationEntry.get());
            businessProcessService.triggerEvent(createWaitForEventName(event, AUTHORIZATION, businessProcess));
        }
    }

    /**
     * Creates the event name for the wait conditions of the business process.
     *
     * @param event                  payment event related to the business process
     * @param paymentTransactionType the payment transaction type
     * @param businessProcess        the business process
     * @return the generated code for the process wait
     */
    protected String createWaitForEventName(final CheckoutComPaymentEventModel event, final PaymentTransactionType paymentTransactionType, final BusinessProcessModel businessProcess) {
        if (PaymentTransactionType.REFUND_FOLLOW_ON.equals(paymentTransactionType)) {
            return businessProcess.getCode() + SEPARATOR + event.getActionId() + SEPARATOR + paymentTransactionType;
        } else {
            return businessProcess.getCode() + SEPARATOR + paymentTransactionType;
        }
    }

    /**
     * Updates the payment event status
     *
     * @param paymentEvents the list of payment events to update
     * @param status        the event status to set
     */
    protected void updateEventStatus(final List<CheckoutComPaymentEventModel> paymentEvents, final CheckoutComPaymentEventStatus status) {
        if (!paymentEvents.isEmpty()) {
            paymentEvents.forEach(paymentEventModel -> paymentEventModel.setStatus(status));
            modelService.saveAll(paymentEvents);
        }
    }

    /**
     * Updates the event with the given errorMessage and adds it to the given list
     *
     * @param errorMessage  error message to add as failed reason
     * @param eventToUpdate event to update
     * @param eventList     list on which the event is added
     */
    protected void updateFailReasonAndAddEventToList(final String errorMessage,
                                                     final CheckoutComPaymentEventModel eventToUpdate,
                                                     final List<CheckoutComPaymentEventModel> eventList) {
        LOG.error(errorMessage);
        eventToUpdate.setFailReason(errorMessage);
        eventList.add(eventToUpdate);
    }

    /**
     * Checks if the event payment id matches the payment info model payment id. If matches the event is valid for
     * processing, otherwise the event can be ignored.
     * If the payment info does not exist, it puts the event in failed
     *
     * @param failedEvents  the failed events list
     * @param ignoredEvents the ignored events list
     * @param event         the event being processed
     * @param order         the related order
     * @return true if the event is valid to process, false otherwise
     */
    protected boolean isEventPaymentIdValidForPaymentInfo(final List<CheckoutComPaymentEventModel> failedEvents, final List<CheckoutComPaymentEventModel> ignoredEvents, final CheckoutComPaymentEventModel event, final OrderModel order) {
        if (order.getPaymentInfo() != null) {
            if (!event.getPaymentId().equalsIgnoreCase(order.getPaymentInfo().getPaymentId())) {
                updateFailReasonAndAddEventToList(String.format("The payment info related to the order with code [%s] does not match the event payment id", order.getCode()),
                        event, ignoredEvents);
                return false;
            }
            return true;
        } else {
            updateFailReasonAndAddEventToList(String.format("The payment info related to the order with code [%s] does not exist.", order.getCode()),
                    event, failedEvents);
            return false;
        }
    }

    /**
     * Checks if the given transaction has already an AUTHORIZATION entry
     *
     * @param paymentTransaction the payment transaction
     * @return true if AUTHORIZATION transaction entry already exists, false otherwise
     */
    protected boolean doesAuthorisationAlreadyExist(final PaymentTransactionModel paymentTransaction) {
        return checkoutComPaymentTransactionService.findAcceptedAuthorizationEntry(paymentTransaction).isPresent() ||
                checkoutComPaymentTransactionService.findPendingAuthorizationEntry(paymentTransaction).isPresent();
    }

    /**
     * Checks if the given event is of type PAYMENT_PENDING or PAYMENT_APPROVED, that means is an AUTHORIZATION event
     *
     * @param paymentEvent the event to check
     * @return true if AUTHORIZATION event, false otherwise
     */
    protected boolean isAuthoriseEventType(final CheckoutComPaymentEventModel paymentEvent) {
        return Stream.of(PAYMENT_PENDING, PAYMENT_APPROVED, PAYMENT_CAPTURE_PENDING)
                .map(CheckoutComPaymentEventType::getCode)
                .anyMatch(status -> status.equalsIgnoreCase(paymentEvent.getEventType()));
    }

    /**
     * Checks if the event type is PAYMENT_EXPIRED or PAYMENT_DECLINED or PAYMENT_CANCELED, that means is an AUTHORIZATION declined
     *
     * @param eventType the event type
     * @return true if the event is of type AUTHORIZATION declined, false otherwise
     */
    protected boolean isAuthorizationDeclinedEvent(final String eventType) {
        return CheckoutComPaymentEventType.PAYMENT_EXPIRED.getCode().equalsIgnoreCase(eventType) ||
                CheckoutComPaymentEventType.PAYMENT_DECLINED.getCode().equalsIgnoreCase(eventType) ||
                CheckoutComPaymentEventType.PAYMENT_CANCELED.getCode().equalsIgnoreCase(eventType);
    }

    /**
     * Processes the events and adds them to completed list if processing was successful, ignored list otherwise
     *
     * @param transactionType payment transaction type
     * @param completedEvents completed event list
     * @param ignoredEvents   ignored event list
     * @param event           payment event
     * @param order           the order for the event payment reference
     * @param businessProcess the business process
     */
    protected void processEventsAndAddToCorrectList(final PaymentTransactionType transactionType,
                                                    final List<CheckoutComPaymentEventModel> completedEvents,
                                                    final List<CheckoutComPaymentEventModel> ignoredEvents,
                                                    final CheckoutComPaymentEventModel event,
                                                    final OrderModel order,
                                                    final List<BusinessProcessModel> businessProcess) {
        final boolean isEventProcessed = processEvent(event, transactionType, order, businessProcess.get(0));
        if (isEventProcessed) {
            completedEvents.add(event);
        } else {
            ignoredEvents.add(event);
        }
    }

    /**
     * If an event has no order but is related to a cart then it will be made pending as the cart may be
     * placed. Otherwise, the event will be marked as failed.
     *
     * @param abstractOrders list of AbstractOrderModel
     * @param event          the payment event
     * @param failedEvents   list of failed events
     */
    protected void handleEventsRelatedToCart(final List<AbstractOrderModel> abstractOrders,
                                             final CheckoutComPaymentEventModel event,
                                             final List<CheckoutComPaymentEventModel> failedEvents) {

        final Optional<AbstractOrderModel> optionalCart = abstractOrders.stream().filter(CartModel.class::isInstance).findAny();

        if (optionalCart.isPresent()) {
            LOG.warn("Found cart model with payment id [{}]. Skipping event processing.", event.getEventId());
        } else {
            updateFailReasonAndAddEventToList(String.format("Order model not found for the following event id [%s] or related transaction null.", event.getEventId()),
                    event, failedEvents);
        }
    }

    protected TransactionOperations getTransactionTemplate() {
        return transactionTemplate;
    }

    protected Optional<CheckoutComPaymentEventModel> findEventInList(final List<CheckoutComPaymentEventModel> eventList, final CheckoutComPaymentEventType eventType) {
        return eventList.stream()
                .filter(event -> eventType.getCode().equalsIgnoreCase(event.getEventType()))
                .findAny();
    }

    protected boolean isDeferredRefund(final CheckoutComPaymentEventModel event, final PaymentTransactionType transactionType) {
        return REFUND_FOLLOW_ON.equals(transactionType) && DEFERRED_EVENT_APPROVED_RESPONSE_CODE.equalsIgnoreCase(event.getResponseCode());
    }
}
