package com.checkout.hybris.core.payment.services;

import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.payments.ResponseSource;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.order.PaymentInfoService;

import java.util.List;

/**
 * Service to handle payment info logic
 */
public interface CheckoutComPaymentInfoService extends PaymentInfoService {

    /**
     * Creates the payment info for given current cart
     *
     * @param paymentInfoModel the payment info to save in the cart
     * @param cartModel        the current cart model
     */
    void createPaymentInfo(PaymentInfoModel paymentInfoModel, CartModel cartModel);

    /**
     * Removes the payment info from the DB and update the cart
     *
     * @param cartModel the current cart
     */
    void removePaymentInfo(CartModel cartModel);

    /**
     * Checks if the credit card payment info model related to the cart is valid or not.
     *
     * @param cartModel the cart model
     * @return true if valid, false otherwise
     */
    boolean isValidCreditCardPaymentInfo(CartModel cartModel);

    /**
     * Checks if the redirect apm payment info model related to the cart is valid or not.
     *
     * @param cartModel the cart model
     * @return true if valid, false otherwise
     */
    boolean isValidRedirectApmPaymentInfo(CartModel cartModel);

    /**
     * Checks if the payment in the given cart is an APM with user data required form.
     *
     * @param cartModel the cart to get the payment info
     * @return true if apm with user data form, false otherwise
     */
    boolean isUserDataRequiredApmPaymentMethod(CartModel cartModel);

    /**
     * Checks if the payment info model related to the cart is valid or not.
     *
     * @param cartModel the cart model
     * @return true if valid, false otherwise
     */
    boolean isValidPaymentInfo(CartModel cartModel);

    /**
     * Adds the QR code data to the Benefit payment info
     *
     * @param paymentInfo payment info
     * @param qrCode      QR code to add
     */
    void addQRCodeDataToBenefitPaymentInfo(CheckoutComBenefitPayPaymentInfoModel paymentInfo, String qrCode);

    /**
     * Adds a payment id to the given payment info model
     *
     * @param paymentId   the checkout.com payment id
     * @param paymentInfo the payment info model
     */
    void addPaymentId(String paymentId, PaymentInfoModel paymentInfo);

    /**
     * Adds the subscription id to the payment info and marks the payment info as saved
     *
     * @param paymentInfo the payment info
     * @param source      the response source from checkout.com
     */
    void addSubscriptionIdToUserPayment(CheckoutComCreditCardPaymentInfoModel paymentInfo, ResponseSource source);

    /**
     * Gets the site id for the given Checkout.com payment id
     *
     * @param paymentId the checkout.com payment id
     * @return site id if paymentInfo with that Id belongs to an AbstractOrder, empty otherwise
     */
    String getSiteIdFromPaymentId(String paymentId);

    /**
     * Finds the abstractOrders that have a paymentInfo matching the paymentId
     *
     * @param paymentId the given paymentId
     * @return List of AbstractOrders
     */
    List<AbstractOrderModel> findAbstractOrderByPaymentId(String paymentId);

    /**
     * Finds the Payment Infos for the given Checkout.com payment id
     *
     * @param paymentId the checkout.com payment id
     * @return List<PaymentInfoModel> belonging to AbstractOrder
     */
    List<PaymentInfoModel> getPaymentInfosByPaymentId(String paymentId);

    /**
     * Set the payloads in the order
     *
     * @param abstractOrder the order
     * @param request the request
     * @param response the response
     */
    void saveRequestAndResponseInOrder(final AbstractOrderModel abstractOrder, final String request, final String response);

    /**
     * Set the payload in the order found by payment reference
     *
     * @param paymentReference the payment reference of the order
     * @param response the response
     */
    void saveResponseInOrderByPaymentReference(final String paymentReference, final String response);

    /**
     * Print in the log the payload for test environments
     *
     * @param payload the payload
     */
    void logInfoOut(final String payload);
}
