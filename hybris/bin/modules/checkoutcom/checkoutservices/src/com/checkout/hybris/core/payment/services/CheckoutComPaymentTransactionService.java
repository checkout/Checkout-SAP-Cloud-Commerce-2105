package com.checkout.hybris.core.payment.services;

import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;

import java.util.Optional;

/**
 * Manages payment transactions
 */
public interface CheckoutComPaymentTransactionService {

    /**
     * Matching order total against sum of amounts from PaymentTransactionEntries with
     * type {@link PaymentTransactionType#AUTHORIZATION}.
     *
     * @param order The current order {@link OrderModel}
     * @return true if sum of amounts are equal to order total. Otherwise false.
     */
    boolean isAuthorisedAmountCorrect(OrderModel order);

    /**
     * Gets the payment reference number from the transaction entry code
     *
     * @param transactionEntryCode the transaction entry code
     * @return the payment reference number
     */
    String getPaymentReferenceFromTransactionEntryCode(String transactionEntryCode);

    /**
     * Gets the first payment transaction for the related order
     *
     * @param order the order model
     * @return the first payment transaction
     */
    PaymentTransactionModel getPaymentTransaction(OrderModel order);

    /**
     * Gets the authorization payment transaction entry in accepted status
     *
     * @param paymentTransaction the payment transaction
     * @return the payment transaction entry model if found
     */
    Optional<PaymentTransactionEntryModel> findAcceptedAuthorizationEntry(PaymentTransactionModel paymentTransaction);

    /**
     * Finds a refund PaymentTransactionEntry in the {@param paymentTransaction} having the specific Checkout.com action id.
     *
     * @param paymentTransaction the payment transaction
     * @param actionId           the action id
     * @return a {@link Optional<PaymentTransactionEntryModel>} payment transaction entry
     */
    Optional<PaymentTransactionEntryModel> findRefundEntryForActionId(PaymentTransactionModel paymentTransaction, String actionId);

    /**
     * Finds a payment transaction related to an event in an order. If the payment transaction cannot be found
     * then a new on will be created
     *
     * @param event the payment event identifying the payment transaction
     * @param order the order which contains the payment transaction
     * @return a {@link Optional<PaymentTransactionModel>} payment transaction
     */
    PaymentTransactionModel findPaymentTransaction(CheckoutComPaymentEventModel event, OrderModel order);

    /**
     * Finds a pending authorization PaymentTransactionEntry in the {@param paymentTransaction}
     *
     * @param paymentTransaction the payment transaction
     * @return a {@link Optional<PaymentTransactionEntryModel>} payment transaction entry
     */
    Optional<PaymentTransactionEntryModel> findPendingAuthorizationEntry(PaymentTransactionModel paymentTransaction);

    /**
     * Creates a payment transaction entry and attaches it to the payment transaction
     *
     * @param paymentTransaction       the payment transaction
     * @param paymentEvent             the checkout.com payment event
     * @param transactionStatus        the transaction status
     * @param transactionStatusDetails the transaction status details
     * @param paymentTransactionType   the payment transaction type
     */
    void createPaymentTransactionEntry(PaymentTransactionModel paymentTransaction, CheckoutComPaymentEventModel paymentEvent, String transactionStatus,
                                       String transactionStatusDetails, PaymentTransactionType paymentTransactionType);
}
