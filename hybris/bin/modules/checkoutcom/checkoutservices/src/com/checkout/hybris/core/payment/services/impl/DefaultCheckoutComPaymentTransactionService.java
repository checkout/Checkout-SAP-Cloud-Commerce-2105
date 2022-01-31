package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.platform.commerceservices.order.CommercePaymentProviderStrategy;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.payment.dto.TransactionStatus.PENDING;
import static de.hybris.platform.payment.enums.PaymentTransactionType.AUTHORIZATION;
import static de.hybris.platform.payment.enums.PaymentTransactionType.REFUND_FOLLOW_ON;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of the {@link CheckoutComPaymentTransactionService}
 */
public class DefaultCheckoutComPaymentTransactionService implements CheckoutComPaymentTransactionService {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentTransactionService.class);

    protected static final String SEPARATOR = "-";
    protected static final String PAYMENT_TRANSACTION_CANNOT_BE_NULL = "Payment transaction cannot be null.";
    protected static final String ORDER_MODEL_CANNOT_BE_NULL = "OrderModel cannot be null";

    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final CommercePaymentProviderStrategy commercePaymentProviderStrategy;
    protected final ModelService modelService;
    protected final TimeService timeService;

    public DefaultCheckoutComPaymentTransactionService(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                       final CommercePaymentProviderStrategy commercePaymentProviderStrategy,
                                                       final ModelService modelService,
                                                       final TimeService timeService) {
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.commercePaymentProviderStrategy = commercePaymentProviderStrategy;
        this.modelService = modelService;
        this.timeService = timeService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorisedAmountCorrect(final OrderModel order) {
        final Double authorisedAmount = getAuthorisedAmount(order);
        final double threshold = checkoutComMerchantConfigurationService.getAuthorisationAmountValidationThreshold(order.getSite().getUid());
        boolean result = Math.abs(order.getTotalPrice() - authorisedAmount) <= threshold;
        LOG.debug("Authorisation amount checked. The result is [{}] for the order amount [{}] does not match the authorisation amount [{}] including threshold [{}].", result, order.getTotalPrice(), authorisedAmount, threshold);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PaymentTransactionEntryModel> findRefundEntryForActionId(final PaymentTransactionModel paymentTransaction, final String actionId) {
        validateParameterNotNull(paymentTransaction, PAYMENT_TRANSACTION_CANNOT_BE_NULL);
        validateParameterNotNull(actionId, "actionId cannot be null.");

        return paymentTransaction.getEntries().stream()
                .filter(entry -> REFUND_FOLLOW_ON.equals(entry.getType()) && entry.getRequestToken().equalsIgnoreCase(actionId))
                .findAny();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionModel findPaymentTransaction(final CheckoutComPaymentEventModel event, final OrderModel order) {
        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            LOG.debug("Creating a new transaction for event with id [{}] and order [{}]", event.getEventId(), order.getCode());
            return createPaymentTransaction(event.getPaymentId(), order, event.getAmount());
        } else {
            LOG.debug("Getting existing transaction for event with id [{}] and order [{}]", event.getEventId(), order.getCode());
            return getPaymentTransaction(order);
        }
    }

    /**
     * Creates a new paymentTransaction on the order
     *
     * @param paymentId the checkout.com unique identifier
     * @param order     the order model
     * @param amount    the amount
     * @return the created {@link PaymentTransactionModel}
     */
    protected PaymentTransactionModel createPaymentTransaction(final String paymentId,
                                                               final OrderModel order,
                                                               final BigDecimal amount) {
        validateParameterNotNull(paymentId, "Payment identifier cannot be null");
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);
        validateParameterNotNull(amount, "Authorised amount cannot be null");

        final PaymentTransactionModel paymentTransactionModel = modelService.create(PaymentTransactionModel.class);
        final String checkoutComPaymentReference = order.getCheckoutComPaymentReference();
        final PaymentInfoModel paymentInfo = order.getPaymentInfo();

        paymentTransactionModel.setCode(checkoutComPaymentReference);
        paymentTransactionModel.setRequestId(paymentId);
        if (paymentInfo instanceof CheckoutComCreditCardPaymentInfoModel) {
            paymentTransactionModel.setRequestToken(((CheckoutComCreditCardPaymentInfoModel) paymentInfo).getCardToken());
        }
        paymentTransactionModel.setPaymentProvider(commercePaymentProviderStrategy.getPaymentProvider());
        paymentTransactionModel.setOrder(order);
        paymentTransactionModel.setCurrency(order.getCurrency());
        paymentTransactionModel.setInfo(paymentInfo);
        paymentTransactionModel.setPlannedAmount(amount);
        modelService.save(paymentTransactionModel);

        return paymentTransactionModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentTransactionModel getPaymentTransaction(final OrderModel order) {
        validateParameterNotNull(order, ORDER_MODEL_CANNOT_BE_NULL);
        checkArgument(CollectionUtils.isNotEmpty(order.getPaymentTransactions()), "Order does not have any payment transaction.");

        if (order.getPaymentTransactions().size() > 1) {
            LOG.warn("Found [{}] payment transactions for order: [{}]", order.getPaymentTransactions().size(), order.getCode());
        }
        return order.getPaymentTransactions().get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPaymentReferenceFromTransactionEntryCode(final String transactionEntryCode) {
        checkArgument(StringUtils.isNotBlank(transactionEntryCode), "Payment transaction entry code cannot be null or empty.");

        return transactionEntryCode.substring(0, transactionEntryCode.indexOf(SEPARATOR, transactionEntryCode.indexOf(SEPARATOR) + 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PaymentTransactionEntryModel> findAcceptedAuthorizationEntry(final PaymentTransactionModel paymentTransaction) {
        validateParameterNotNull(paymentTransaction, PAYMENT_TRANSACTION_CANNOT_BE_NULL);

        return paymentTransaction.getEntries().stream()
                .filter(this::isTransactionEntryAuthorized).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PaymentTransactionEntryModel> findPendingAuthorizationEntry(final PaymentTransactionModel paymentTransaction) {
        validateParameterNotNull(paymentTransaction, PAYMENT_TRANSACTION_CANNOT_BE_NULL);

        return paymentTransaction.getEntries().stream()
                .filter(this::isAuthorizationTransactionEntryPending).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPaymentTransactionEntry(final PaymentTransactionModel paymentTransaction,
                                              final CheckoutComPaymentEventModel paymentEvent,
                                              final String transactionStatus,
                                              final String transactionStatusDetails,
                                              final PaymentTransactionType paymentTransactionType) {
        validateParameterNotNull(paymentTransaction, "Payment transaction cannot be null");
        validateParameterNotNull(paymentEvent, "Payment event cannot be null");
        validateParameterNotNull(paymentEvent.getCurrency(), "Currency cannot be null");
        validateParameterNotNull(paymentEvent.getAmount(), "Authorised amount cannot be null");
        validateParameterNotNull(transactionStatus, "Transaction status cannot be null");
        validateParameterNotNull(transactionStatusDetails, "Transaction status details cannot be null");
        validateParameterNotNull(paymentTransactionType, "Transaction type cannot be null");

        final PaymentTransactionEntryModel transactionEntryModel = modelService.create(PaymentTransactionEntryModel.class);
        transactionEntryModel.setCode(getNewPaymentTransactionEntryCode(paymentTransaction, paymentTransactionType));
        transactionEntryModel.setRequestId(paymentTransaction.getRequestId());
        transactionEntryModel.setType(paymentTransactionType);
        transactionEntryModel.setPaymentTransaction(paymentTransaction);
        transactionEntryModel.setRequestToken(paymentEvent.getActionId());
        transactionEntryModel.setTime(timeService.getCurrentTime());
        transactionEntryModel.setTransactionStatus(transactionStatus);
        transactionEntryModel.setTransactionStatusDetails(transactionStatusDetails);
        transactionEntryModel.setAmount(paymentEvent.getAmount());
        transactionEntryModel.setCurrency(paymentEvent.getCurrency());

        modelService.save(transactionEntryModel);
        modelService.refresh(paymentTransaction);
    }

    /**
     * Default implementation of the authorised amount calculation. It assumes there is only one payment method used
     * to pay for the order (one payment transaction). If multiple payment methods used (such as gift card and/or store credit)
     * the amount returned will have to not consider them.
     *
     * @param order the order we want to get the authorised amount for
     * @return the authorised amount
     */
    protected Double getAuthorisedAmount(final OrderModel order) {
        final PaymentTransactionModel paymentTransaction = getPaymentTransaction(order);
        return paymentTransaction.getEntries().stream()
                .filter(entry -> AUTHORIZATION.equals(entry.getType()))
                .map(PaymentTransactionEntryModel::getAmount)
                .collect(Collectors.summarizingDouble(BigDecimal::doubleValue)).getSum();
    }

    protected boolean isAuthorizationTransactionEntryPending(final PaymentTransactionEntryModel paymentTransactionEntry) {
        return AUTHORIZATION.equals(paymentTransactionEntry.getType()) &&
                PENDING.toString().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus());
    }

    protected boolean isTransactionEntryAuthorized(final PaymentTransactionEntryModel paymentTransactionEntry) {
        return AUTHORIZATION.equals(paymentTransactionEntry.getType()) &&
                (TransactionStatus.ACCEPTED.toString().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus()) ||
                        TransactionStatus.REVIEW.toString().equalsIgnoreCase(paymentTransactionEntry.getTransactionStatus()));
    }

    protected String getNewPaymentTransactionEntryCode(PaymentTransactionModel transaction, PaymentTransactionType paymentTransactionType) {
        return transaction.getEntries() == null ? transaction.getCode() + "-" + paymentTransactionType.getCode() + "-1" : transaction.getCode() + "-" + paymentTransactionType.getCode() + "-" + (transaction.getEntries().size() + 1);
    }
}
