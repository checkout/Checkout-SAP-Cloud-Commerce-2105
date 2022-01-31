package com.checkout.hybris.fulfilmentprocess.actions.voids;

import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentTransactionService;
import com.checkout.hybris.fulfilmentprocess.model.CheckoutComVoidProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractAction;
import de.hybris.platform.task.RetryLaterException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

/**
 * Checkout.com action to cancel an Order
 */
public class CheckoutComVoidOrderAction extends AbstractAction<CheckoutComVoidProcessModel> {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComVoidOrderAction.class);

    protected static final String OK = "OK";
    protected static final String NOK = "NOK";
    protected static final String WAIT = "WAIT";

    protected final CheckoutComPaymentService paymentService;
    protected final CheckoutComPaymentTransactionService checkoutComPaymentTransactionService;

    public CheckoutComVoidOrderAction(final CheckoutComPaymentService paymentService,
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
    public String execute(final CheckoutComVoidProcessModel process) {
        final OrderModel order = process.getOrder();

        if (order != null && CollectionUtils.isNotEmpty(order.getPaymentTransactions())) {
            if (paymentService.isVoidPresent(order)) {
                LOG.info("Order with code [{}] has been already voided.", order.getCode());
                return OK;
            }

            final PaymentTransactionModel paymentTransaction = checkoutComPaymentTransactionService.getPaymentTransaction(order);
            final Optional<PaymentTransactionEntryModel> authorizationTransactionEntryAccepted = checkoutComPaymentTransactionService.findAcceptedAuthorizationEntry(paymentTransaction);
            final PaymentTransactionEntryModel transactionEntry;

            if (authorizationTransactionEntryAccepted.isPresent()) {
                try {
                    transactionEntry = paymentService.cancel(authorizationTransactionEntryAccepted.get());
                } catch (final CheckoutComPaymentIntegrationException e) {
                    LOG.error("Payment integration exception [{}]. Retrying...", e.getMessage());
                    throw new RetryLaterException("Payment Gateway exception during void.");
                }
                return evaluateProcessOutcomeForVoidPayment(transactionEntry, order, paymentTransaction);
            } else {
                LOG.error("Accepted authorization entry not present for order with code [{}] and process [{}]", order.getCode(), process.getCode());
                return NOK;
            }
        }

        LOG.error("Order not found for order process with code [{}] or transaction not present.", process.getCode());
        return NOK;
    }

    /**
     * Decides what is the outcome of the process based on the void payment response
     *
     * @param transactionEntry   the entry returned but the payment capture
     * @param order              the order model
     * @param paymentTransaction the payment transaction
     * @return process outcome
     */
    protected String evaluateProcessOutcomeForVoidPayment(final PaymentTransactionEntryModel transactionEntry, final OrderModel order,
                                                          final PaymentTransactionModel paymentTransaction) {
        if (TransactionStatus.ACCEPTED.name().equals(transactionEntry.getTransactionStatus())) {
            LOG.info("The void payment has been completed. Order: [{}]. Transaction: [{}]",
                    order.getCode(), paymentTransaction.getCode());
            return OK;
        } else if (TransactionStatus.ERROR.name().equals(transactionEntry.getTransactionStatus()) || TransactionStatus.REJECTED.name().equals(transactionEntry.getTransactionStatus())) {
            LOG.error("The void payment has failed. Order: [{}]. Transaction: [{}]. Transaction Status: [{}]",
                    order.getCode(), paymentTransaction.getCode(), transactionEntry.getTransactionStatus());
            return NOK;
        } else {
            LOG.info("The void payment is still pending for order: [{}] and transaction code: [{}]",
                    order.getCode(), paymentTransaction.getCode());
            return WAIT;
        }
    }
}
