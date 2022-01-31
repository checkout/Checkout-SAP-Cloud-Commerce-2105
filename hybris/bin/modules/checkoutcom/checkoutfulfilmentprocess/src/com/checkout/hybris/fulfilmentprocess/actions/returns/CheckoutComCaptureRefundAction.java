/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.checkout.hybris.fulfilmentprocess.actions.returns;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.hybris.platform.payment.dto.TransactionStatus.valueOf;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static jersey.repackaged.com.google.common.base.Preconditions.checkArgument;


/**
 * Sample implementation for refunding the money to the customer for the ReturnRequest.
 * Use this as a starting point to fulfil your refund business requirements.
 * <p>
 * Assumptions:
 * - Only one PaymentTransaction exists for the Order
 * - Simple calculation of refund amount
 */
public class CheckoutComCaptureRefundAction extends AbstractAction<ReturnProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComCaptureRefundAction.class);

    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    protected final CheckoutComPaymentService paymentService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComCaptureRefundAction(final CheckoutComPaymentService paymentService,
                                          final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService) {
        this.paymentService = paymentService;
        this.checkoutComPaymentTransactionService = checkoutComPaymentTransactionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getTransitions() {
        return AbstractAction.createTransitions(OK, NOK, WAIT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute(final ReturnProcessModel process) throws RetryLaterException {
        LOG.debug("Process: {} in step {}", process.getCode(), getClass().getSimpleName());

        final ReturnRequestModel returnRequest = process.getReturnRequest();
        final OrderModel order = returnRequest.getOrder();
        final List<PaymentTransactionModel> paymentTransactions = order.getPaymentTransactions();

        if (paymentTransactions.isEmpty()) {
            LOG.info("Unable to refund for ReturnRequest [{}], no PaymentTransactions found", returnRequest.getCode());
            setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSAL_FAILED);
            return NOK;
        }

        final PaymentTransactionModel paymentTransaction = checkoutComPaymentTransactionService.getPaymentTransaction(order);

        final PaymentInfoModel paymentInfo = paymentTransaction.getInfo();
        if (paymentInfo instanceof CheckoutComCreditCardPaymentInfoModel || paymentInfo instanceof CheckoutComAPMPaymentInfoModel) {

            String refundActionId = process.getRefundActionId();
            PaymentTransactionEntryModel transactionEntry;

            if (StringUtils.isBlank(refundActionId)) {
                LOG.info("Initiating refund for order: [{}]", order.getCode());

                transactionEntry = refundPayment(paymentTransaction, getRefundAmount(returnRequest));
                refundActionId = transactionEntry.getRequestToken();

                LOG.info("Refund response received with actionId: [{}]", refundActionId);
                process.setRefundActionId(refundActionId);
                save(process);
            } else {
                final Optional<PaymentTransactionEntryModel> optionalRefundEntry = checkoutComPaymentTransactionService.findRefundEntryForActionId(paymentTransaction, refundActionId);

                if (optionalRefundEntry.isEmpty()) {
                    LOG.error("Can't find a refund entry for transaction: [{}] and actionId: [{}].", paymentTransaction.getCode(), refundActionId);
                    return NOK;
                }

                transactionEntry = optionalRefundEntry.get();
            }

            return evaluateProcessOutcome(returnRequest, paymentTransaction, transactionEntry);
        }

        return OK;
    }

    /**
     * Initiates a follow-on refund and returns the request token
     *
     * @param transaction    the payment transaction of the order
     * @param amountToRefund the amount to be refunded
     * @return the refund request token
     */
    protected PaymentTransactionEntryModel refundPayment(final PaymentTransactionModel transaction, final BigDecimal amountToRefund) {
        try {
            return paymentService.refundFollowOn(transaction, amountToRefund);
        } catch (final CheckoutComPaymentIntegrationException e) {
            LOG.error("Payment integration exception [{}]. Retrying...", e.getMessage());
            throw new RetryLaterException("Payment Gateway exception during refund process.");
        }
    }

    /**
     * Checks the state of the refund transaction entry and returns the outcome for the return process
     * <p>
     * In case of:
     * - PENDING the action should wait for the event notification to be processed
     * - ACCEPTED the action should carry on with OK
     * - ERROR the action should exit with NOK as it means the refund has failed
     * - any other case exits with NOK
     *
     * @param returnRequest           the return request for this process
     * @param paymentTransaction      the order's payment transaction
     * @param paymentTransactionEntry the refund payment transaction entry
     * @return the process transition
     */
    protected String evaluateProcessOutcome(final ReturnRequestModel returnRequest, final PaymentTransactionModel paymentTransaction, final PaymentTransactionEntryModel paymentTransactionEntry) {
        String outcome;

        final String transactionStatus = paymentTransactionEntry.getTransactionStatus();

        switch (valueOf(transactionStatus)) {
            case PENDING:
                LOG.info("The refund is in PENDING state for return request: [{}] and transaction: [{}]", returnRequest.getCode(), paymentTransaction.getCode());
                setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSAL_PENDING);
                outcome = WAIT;
                break;
            case ACCEPTED:
                LOG.info("The refund is in ACCEPTED state for return request: [{}] and transaction: [{}]", returnRequest.getCode(), paymentTransaction.getCode());
                setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSED);
                outcome = OK;
                break;
            case ERROR:
                LOG.info("The refund is in ERROR state for return request: [{}] and transaction: [{}]", returnRequest.getCode(), paymentTransaction.getCode());
                setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSAL_FAILED);
                outcome = NOK;
                break;
            default:
                LOG.error("The refund is in an unsupported state [{}] for return request: [{}] and transaction: [{}]", transactionStatus, returnRequest.getCode(), paymentTransaction.getCode());
                setReturnRequestStatus(returnRequest, ReturnStatus.PAYMENT_REVERSAL_FAILED);
                outcome = NOK;
                break;
        }

        return outcome;
    }

    /**
     * Simple calculation of the amount to be refunded. Use this as an example and adjust to your business requirements
     *
     * @param returnRequest the return request
     * @return the amount to refund
     */
    protected BigDecimal getRefundAmount(final ReturnRequestModel returnRequest) {
        checkArgument(CollectionUtils.isNotEmpty(returnRequest.getReturnEntries()), "Parameter Return Entries cannot be null");

        BigDecimal refundAmount = returnRequest.getReturnEntries().stream()
                .map(this::getRefundEntryAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (returnRequest.getRefundDeliveryCost()) {
            refundAmount = refundAmount.add(BigDecimal.valueOf(returnRequest.getOrder().getDeliveryCost()));
        }

        return refundAmount.setScale(getNumberOfDigits(returnRequest), RoundingMode.CEILING);
    }

    protected BigDecimal getRefundEntryAmount(final ReturnEntryModel returnEntryModel) {
        validateParameterNotNull(returnEntryModel, "Parameter Return Entry cannot be null");
        final ReturnRequestModel returnRequest = returnEntryModel.getReturnRequest();
        BigDecimal refundEntryAmount = BigDecimal.ZERO;

        if (returnEntryModel instanceof RefundEntryModel) {
            final RefundEntryModel refundEntry = (RefundEntryModel) returnEntryModel;

            refundEntryAmount = refundEntry.getAmount();
            refundEntryAmount = refundEntryAmount.setScale(getNumberOfDigits(returnRequest), RoundingMode.HALF_DOWN);
        }
        return refundEntryAmount;
    }

    /**
     * Update the return status for all return entries in {@link ReturnRequestModel}
     *
     * @param returnRequest the return request
     * @param status        the return status
     */
    protected void setReturnRequestStatus(final ReturnRequestModel returnRequest, final ReturnStatus status) {
        returnRequest.setStatus(status);
        returnRequest.getReturnEntries().forEach(entry -> {
            entry.setStatus(status);
            save(entry);
        });
        save(returnRequest);
    }

    protected int getNumberOfDigits(final ReturnRequestModel returnRequest) {
        return returnRequest.getOrder().getCurrency().getDigits();
    }
}
