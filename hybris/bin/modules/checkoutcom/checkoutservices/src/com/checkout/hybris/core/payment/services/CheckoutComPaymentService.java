package com.checkout.hybris.core.payment.services;

import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.events.model.CheckoutComPaymentEventModel;
import com.checkout.payments.PaymentPending;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.payment.PaymentService;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionModel;

/**
 * Manages payments and payment transactions
 */
public interface CheckoutComPaymentService extends PaymentService {

    /**
     * Checks whether the order contains an authorization payment transaction entry which is still pending.
     * The entry is pending if a PaymentTransaction could not be found or the PaymentTransaction contains no
     * authorization PaymentTransactionEntry
     *
     * @param order the order to check
     * @return true if the authorization is pending or false otherwise
     */
    boolean isAuthorizationPending(OrderModel order);

    /**
     * Checks whether the order contains a capture payment transaction entry which is still pending.
     * The entry is pending if a PaymentTransaction could not be found or the PaymentTransaction contains no
     * capture PaymentTransactionEntry
     *
     * @param order the order to check
     * @return true if the capture is pending or false otherwise
     */
    boolean isCapturePending(OrderModel order);

    /**
     * Determines if order payment is supposed to be auto-captured
     *
     * @param order the order to check
     * @return true if auto-capture is expected of false otherwise
     */
    boolean isAutoCapture(OrderModel order);

    /**
     * Determines if order payment is supposed to be deferred
     *
     * @param order the order to check
     * @return true if deferred is expected of false otherwise
     */
    boolean isDeferred(OrderModel order);

    /**
     * Checks whether the order contains an accepted or review authorisation payment transaction entry
     *
     * @param order the order to check
     * @return true if there is an accepted authorization payment transaction entry, false otherwise
     */
    boolean isAuthorizationApproved(OrderModel order);

    /**
     * Checks whether the order contains an accepted capture payment transaction entry
     *
     * @param order the order to check
     * @return true if there is an accepted capture payment transaction entry, false otherwise
     */
    boolean isCaptureApproved(OrderModel order);

    /**
     * Checks whether the order contains a void authorisation payment transaction entry
     *
     * @param order the order to check
     * @return true if there is a void authorization payment transaction entry, false otherwise
     */
    boolean isVoidPresent(OrderModel order);

    /**
     * Checks whether the order contains a pending void payment transaction entry
     *
     * @param order the order to check
     * @return true if there is a pending void payment transaction entry, false otherwise
     */
    boolean isVoidPending(OrderModel order);

    /**
     * Checks if any of the PaymentTransactionEntries order is of type capture.
     *
     * @param order the order model
     * @return true if any of the entries is capture. False otherwise.
     */
    boolean captureExists(OrderModel order);

    /**
     * Accepts a payment based on the event. A payment transaction entry will represent the outcome of the payment acceptance
     *
     * @param paymentEvent    the payment event representing an accepted payment
     * @param transaction     the payment transaction related to the payment
     * @param transactionType the payment transaction type
     */
    void acceptPayment(CheckoutComPaymentEventModel paymentEvent, PaymentTransactionModel transaction, PaymentTransactionType transactionType);

    /**
     * Rejects a payment based on the event. A payment transaction entry will represent the outcome of the payment acceptance
     *
     * @param paymentEvent    the payment event representing a rejected payment
     * @param transaction     the payment transaction related to the payment
     * @param transactionType the payment transaction type
     */
    void rejectPayment(CheckoutComPaymentEventModel paymentEvent, PaymentTransactionModel transaction, PaymentTransactionType transactionType);

    /**
     * Return a payment based on the event. A payment transaction entry will represent the outcome of the payment return
     *
     * @param paymentEvent    the payment event representing an accepted payment
     * @param transaction     the payment transaction related to the payment
     * @param transactionType the payment transaction type
     */
    void returnPayment(CheckoutComPaymentEventModel paymentEvent, PaymentTransactionModel transaction, PaymentTransactionType transactionType);

    /**
     * Handles the pending payment response based on the APM payment type and returns the authorise response
     *
     * @param paymentPendingResponse the pending payment response from checkout.com
     * @param paymentInfo            the payment info model
     * @return the AuthorizeResponse with populated results
     */
    AuthorizeResponse handlePendingPaymentResponse(PaymentPending paymentPendingResponse, PaymentInfoModel paymentInfo);
}
