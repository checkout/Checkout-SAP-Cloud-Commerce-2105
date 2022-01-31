package com.checkout.hybris.fulfilmentprocess.actions.order;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * The TakePayment step captures the payment transaction.
 */
public class CheckoutComTakePaymentAction extends AbstractAction<OrderProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComTakePaymentAction.class);

    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    protected final CheckoutComPaymentService paymentService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComTakePaymentAction(final CheckoutComPaymentService paymentService,
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
    public String execute(final OrderProcessModel process) throws RetryLaterException {
        final OrderModel order = process.getOrder();

        if (order == null) {
            LOG.error("No order found for order process: [{}]", process.getCode());
            return NOK;
        }

        if (CollectionUtils.isEmpty(order.getPaymentTransactions())) {
            LOG.error("No payment payment transaction found for order: [{}]", order.getCode());
            setOrderStatus(order, OrderStatus.PAYMENT_NOT_CAPTURED);
            return NOK;
        }

        final PaymentTransactionModel paymentTransaction = checkoutComPaymentTransactionService.getPaymentTransaction(order);

        final PaymentInfoModel paymentInfo = paymentTransaction.getInfo();

        if (paymentInfo instanceof CheckoutComCreditCardPaymentInfoModel || paymentInfo instanceof CheckoutComAPMPaymentInfoModel) {
            if (paymentService.captureExists(order)) {
                return evaluateProcessOutcomeForOrder(order);
            } else {
                return handleProcessNotCaptured(order, paymentTransaction);
            }
        }

        return OK;
    }

    /**
     * Handle the process when a payment capture transaction entry does not exist. It can be due to two circumstances:
     * 1. Order is auto-capture and the system needs to wait for the notification to be processed
     * 2. Order is not auto-capture and the system needs to invoke the capture
     *
     * @param order              the order
     * @param paymentTransaction the payment transaction
     * @return the transaction status
     */
    protected String handleProcessNotCaptured(final OrderModel order, final PaymentTransactionModel paymentTransaction) {
        if (paymentService.isAutoCapture(order) || paymentService.isDeferred(order)) {
            return evaluateProcessOutcomeForOrder(order);
        } else {
            return capturePayment(order, paymentTransaction);
        }
    }

    /**
     * Performs the payment capture
     *
     * @param order              the order
     * @param paymentTransaction the payment transaction
     * @return the transaction status
     */
    protected String capturePayment(final OrderModel order, final PaymentTransactionModel paymentTransaction) {
        final PaymentTransactionEntryModel transactionEntry;
        try {
            transactionEntry = paymentService.capture(paymentTransaction);
        } catch (final CheckoutComPaymentIntegrationException e) {
            LOG.error("Payment integration exception [{}]. Retrying...", e.getMessage());
            throw new RetryLaterException("Payment Gateway exception during capture.");
        }

        return evaluateProcessOutcomeForPaymentCapture(transactionEntry, order, paymentTransaction);
    }

    /**
     * Decides what is the outcome of the process based on the payment capture response
     *
     * @param transactionEntry   the entry returned but the payment capture
     * @param order              the order model
     * @param paymentTransaction the payment transaction
     * @return process outcome
     */
    protected String evaluateProcessOutcomeForPaymentCapture(final PaymentTransactionEntryModel transactionEntry, final OrderModel order,
                                                             final PaymentTransactionModel paymentTransaction) {
        if (TransactionStatus.ACCEPTED.name().equals(transactionEntry.getTransactionStatus())) {
            LOG.info("The payment transaction has been captured. Order: [{}]. Transaction: [{}]",
                    order.getCode(), paymentTransaction.getCode());
            setOrderStatus(order, OrderStatus.PAYMENT_CAPTURED);
            return OK;
        } else if (TransactionStatus.ERROR.name().equals(transactionEntry.getTransactionStatus()) || TransactionStatus.REJECTED.name().equals(transactionEntry.getTransactionStatus())) {
            LOG.error("The payment transaction capture has failed. Order: [{}]. Transaction: [{}]. Transaction Status: [{}]",
                    order.getCode(), paymentTransaction.getCode(), transactionEntry.getTransactionStatus());
            setOrderStatus(order, OrderStatus.PAYMENT_NOT_CAPTURED);
            return NOK;
        } else {
            LOG.info("The payment capture is still pending for order: [{}] and transaction code: [{}]",
                    order.getCode(), paymentTransaction.getCode());
            setOrderStatus(order, OrderStatus.CAPTURE_PENDING);
            return WAIT;
        }
    }

    /**
     * Decides what is the outcome of the process based on state of the order
     *
     * @param order the order
     * @return process outcome
     */
    protected String evaluateProcessOutcomeForOrder(final OrderModel order) {
        if (paymentService.isCaptureApproved(order)) {
            LOG.info("The payment has already been captured and it's in ACCEPTED state for order [{}].]", order.getCode());
            setOrderStatus(order, OrderStatus.PAYMENT_CAPTURED);
            return OK;
        } else if (paymentService.isCapturePending(order)) {
            LOG.info("The payment has already been captured but is in PENDING state for order [{}].", order.getCode());
            setOrderStatus(order, OrderStatus.CAPTURE_PENDING);
            return WAIT;
        } else {
            LOG.error("The payment capture is in failed state for order: [{}].", order.getCode());
            setOrderStatus(order, OrderStatus.PAYMENT_NOT_CAPTURED);
            return NOK;
        }
    }

}
