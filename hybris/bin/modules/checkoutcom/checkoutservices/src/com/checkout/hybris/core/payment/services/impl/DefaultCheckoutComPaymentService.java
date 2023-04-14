package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.core.payment.response.mappers.CheckoutComPaymentResponseStrategyMapper;
import com.checkout.hybris.core.payment.response.strategies.CheckoutComPaymentResponseStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentReturnedService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.events.enums.CheckoutComPaymentEventType;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.payments.PaymentPending;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.impl.DefaultPaymentServiceImpl;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static de.hybris.platform.payment.dto.TransactionStatus.*;
import static de.hybris.platform.payment.dto.TransactionStatusDetails.*;
import static de.hybris.platform.payment.enums.PaymentTransactionType.*;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of the {@link CheckoutComPaymentService}
 */
public class DefaultCheckoutComPaymentService extends DefaultPaymentServiceImpl implements CheckoutComPaymentService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentService.class);

    protected static final String ORDER_MODEL_CANNOT_BE_NULL = "OrderModel cannot be null";
    protected static final String PAYMENT_ID_CANNOT_BE_NULL = "PaymentId cannot be null.";
    protected static final String TRANSACTION_CANNOT_BE_NULL = "Transaction cannot be null.";
    protected static final String TRANSACTION_TYPE_CANNOT_BE_NULL = "TransactionType cannot be null.";

    protected final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper;
    protected final CheckoutComPaymentReturnedService checkoutComPaymentReturnedService;

    public DefaultCheckoutComPaymentService(final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver,
                                            final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService,
                                            final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                            final CheckoutComPaymentResponseStrategyMapper checkoutComPaymentResponseStrategyMapper,
                                            CheckoutComPaymentReturnedService checkoutComPaymentReturnedService) {
        this.checkoutComPaymentTypeResolver = checkoutComPaymentTypeResolver;
        this.checkoutComPaymentTransactionService = checkoutComPaymentTransactionService;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.checkoutComPaymentResponseStrategyMapper = checkoutComPaymentResponseStrategyMapper;
        this.checkoutComPaymentReturnedService = checkoutComPaymentReturnedService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorizationPending(final OrderModel order) {
        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            return true;
        }

        boolean isPendingAuthorizationPaymentTransaction = true;
        final PaymentTransactionModel paymentTransaction = checkoutComPaymentTransactionService.getPaymentTransaction(order);
        if (isAuthorizationTransactionEntryNotPending(paymentTransaction)) {
            isPendingAuthorizationPaymentTransaction = false;
        }
        return isPendingAuthorizationPaymentTransaction;
    }

    /**
     * Checks if any of the PaymentTransactionEntries in the {@param paymentTransaction} is authorization type
     * and is not pending.
     *
     * @param paymentTransaction The current {@link PaymentTransactionModel}
     * @return true if any of the entries is authorization and not pending. False otherwise.
     */
    protected boolean isAuthorizationTransactionEntryNotPending(final PaymentTransactionModel paymentTransaction) {
        return paymentTransaction.getEntries().stream()
                .anyMatch(paymentTransactionEntry -> AUTHORIZATION.equals(paymentTransactionEntry.getType()) &&
                        !PENDING.name().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorizationApproved(final OrderModel order) {
        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            return false;
        }

        return checkoutComPaymentTransactionService.getPaymentTransaction(order).getEntries().stream()
                .anyMatch(this::isTransactionEntryAuthorized);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCapturePending(final OrderModel order) {
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);

        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            return true;
        }

        return !captureExists(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean captureExists(final OrderModel order) {
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);

        return checkoutComPaymentTransactionService.getPaymentTransaction(order).getEntries().stream()
                .anyMatch(paymentTransactionEntry -> CAPTURE.equals(paymentTransactionEntry.getType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptPayment(final CheckoutComPaymentEventModel paymentEvent, final PaymentTransactionModel transaction, final PaymentTransactionType transactionType) {
        validateParameterNotNull(paymentEvent, PAYMENT_ID_CANNOT_BE_NULL);
        validateParameterNotNull(transaction, TRANSACTION_CANNOT_BE_NULL);
        validateParameterNotNull(transactionType, TRANSACTION_TYPE_CANNOT_BE_NULL);

        final Optional<PaymentTransactionEntryModel> paymentTransactionEntryOptional = findPendingTransactionEntry(paymentEvent.getPaymentId(), transaction, transactionType);

        if (paymentTransactionEntryOptional.isPresent()) {
            LOG.debug("Accepting pending transaction entry of type [{}] for event with id [{}]", transactionType, paymentEvent.getEventId());
            final PaymentTransactionEntryModel paymentTransactionEntry = paymentTransactionEntryOptional.get();
            paymentTransactionEntry.setTransactionStatus(ACCEPTED.name());
            getModelService().save(paymentTransactionEntry);
        } else {
            String transactionStatus;
            String transactionStatusDetails;

            if (CheckoutComPaymentEventType.PAYMENT_PENDING.getCode().equalsIgnoreCase(paymentEvent.getEventType())) {
                transactionStatus = PENDING.name();
                transactionStatusDetails = SUCCESFULL.name();
            } else {
                final AbstractOrderModel order = transaction.getOrder();
                final String siteId = order.getSite().getUid();
                final boolean shouldPutTransactionInReview = paymentEvent.getRiskFlag() && checkoutComMerchantConfigurationService.isReviewTransactionsAtRisk(siteId);

                transactionStatus = shouldPutTransactionInReview ? REVIEW.name() : ACCEPTED.name();
                transactionStatusDetails = shouldPutTransactionInReview ? REVIEW_NEEDED.name() : SUCCESFULL.name();
                order.setStatus(OrderStatus.PAYMENT_CAPTURED);
                getModelService().save(order);
            }

            LOG.debug("Creating a new transaction entry of type [{}] based on event with id [{}]", transactionType, paymentEvent.getEventId());
            checkoutComPaymentTransactionService.createPaymentTransactionEntry(transaction, paymentEvent, transactionStatus, transactionStatusDetails, transactionType);
        }
    }

    @Override
    public void returnPayment(final CheckoutComPaymentEventModel paymentEvent, final PaymentTransactionModel transaction, final PaymentTransactionType transactionType) {
        validateParameterNotNull(paymentEvent, PAYMENT_ID_CANNOT_BE_NULL);
        validateParameterNotNull(transaction, TRANSACTION_CANNOT_BE_NULL);
        validateParameterNotNull(transactionType, TRANSACTION_TYPE_CANNOT_BE_NULL);

        final AbstractOrderModel order = transaction.getOrder();

        order.setStatus(OrderStatus.PAYMENT_RETURNED);
        getModelService().save(order);

        checkoutComPaymentReturnedService.handlePaymentReturned(order);

        LOG.debug("Creating a new transaction entry of type [{}] based on event with id [{}]", transactionType, paymentEvent.getEventId());
        checkoutComPaymentTransactionService.createPaymentTransactionEntry(transaction, paymentEvent, ACCEPTED.name(), SUCCESFULL.name(), transactionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectPayment(final CheckoutComPaymentEventModel paymentEvent, final PaymentTransactionModel transaction, final PaymentTransactionType transactionType) {
        validateParameterNotNull(paymentEvent, PAYMENT_ID_CANNOT_BE_NULL);
        validateParameterNotNull(transaction, TRANSACTION_CANNOT_BE_NULL);
        validateParameterNotNull(transactionType, TRANSACTION_TYPE_CANNOT_BE_NULL);

        LOG.debug("Creating a new REJECTED transaction entry of type [{}] based on event with id [{}]", transactionType, paymentEvent.getEventId());
        checkoutComPaymentTransactionService.createPaymentTransactionEntry(transaction, paymentEvent, REJECTED.name(), PROCESSOR_DECLINE.name(), transactionType);
    }

    /**
     * Finds a payment transaction entry within a payment transaction of a certain type and matching a payment id
     *
     * @param paymentId       the payment id
     * @param transaction     the payment transaction
     * @param transactionType the transaction type
     * @return the {@link Optional<PaymentTransactionEntryModel>}
     */
    protected Optional<PaymentTransactionEntryModel> findPendingTransactionEntry(final String paymentId, final PaymentTransactionModel transaction, final PaymentTransactionType transactionType) {
        return transaction.getEntries().stream()
                .filter(entry -> transactionType.equals(entry.getType())
                        && entry.getRequestId().equalsIgnoreCase(paymentId)
                        && entry.getTransactionStatus().equalsIgnoreCase(TransactionStatus.PENDING.toString()))
                .findAny();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAutoCapture(final OrderModel order) {
        if (order == null) {
            return false;
        }

        final PaymentInfoModel paymentInfo = order.getPaymentInfo();
        if (paymentInfo instanceof CheckoutComCreditCardPaymentInfoModel) {
            return ((CheckoutComCreditCardPaymentInfoModel) order.getPaymentInfo()).getAutoCapture();
        } else {
            return order.getSite().getCheckoutComMerchantConfiguration().getUseNas();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeferred(final OrderModel order) {
        validateParameterNotNull(order, "Order cannot be null");
        validateParameterNotNull(order.getPaymentInfo(), "Payment info cannot be null");

        return order.getPaymentInfo() instanceof CheckoutComAPMPaymentInfoModel && ((CheckoutComAPMPaymentInfoModel) order.getPaymentInfo()).getDeferred();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCaptureApproved(final OrderModel order) {
        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            return false;
        }

        return checkoutComPaymentTransactionService.getPaymentTransaction(order).getEntries().stream()
                .anyMatch(paymentTransactionEntry -> CAPTURE.equals(paymentTransactionEntry.getType()) &&
                        TransactionStatus.ACCEPTED.toString().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus()));
    }

    protected boolean isTransactionEntryAuthorized(final PaymentTransactionEntryModel paymentTransactionEntry) {
        return AUTHORIZATION.equals(paymentTransactionEntry.getType()) &&
                (TransactionStatus.ACCEPTED.toString().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus()) ||
                        TransactionStatus.REVIEW.toString().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVoidPresent(final OrderModel order) {
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);

        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            return false;
        }

        return checkoutComPaymentTransactionService.getPaymentTransaction(order).getEntries().stream()
                .anyMatch(paymentTransactionEntry -> CANCEL.equals(paymentTransactionEntry.getType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVoidPending(final OrderModel order) {
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);
        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            return false;
        }
        return checkoutComPaymentTransactionService.getPaymentTransaction(order).getEntries().stream()
                .filter(paymentTransactionEntry -> CANCEL.equals(paymentTransactionEntry.getType()))
                .anyMatch(transactionEntry -> PENDING.name().equalsIgnoreCase(transactionEntry.getTransactionStatus()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizeResponse handlePendingPaymentResponse(final PaymentPending paymentPendingResponse, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentPendingResponse, "Payment response cannot be null");
        validateParameterNotNull(paymentInfo, "Payment info model cannot be null");

        final CheckoutComPaymentType paymentType = checkoutComPaymentTypeResolver.resolvePaymentType(paymentInfo);
        final CheckoutComPaymentResponseStrategy paymentResponseStrategy = checkoutComPaymentResponseStrategyMapper.findStrategy(paymentType);
        return paymentResponseStrategy.handlePendingPaymentResponse(paymentPendingResponse, paymentInfo);
    }
}
