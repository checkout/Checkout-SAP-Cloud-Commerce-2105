package com.checkout.hybris.facades.payment;

import com.checkout.payments.GetPaymentResponse;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;

/**
 * Handles payment info facade logic
 */
public interface CheckoutComPaymentInfoFacade {

    /**
     * Adds the payment info to the current cart
     *
     * @param paymentInfoData the payment method info
     */
    void addPaymentInfoToCart(Object paymentInfoData);

    /**
     * Creates a specific payment info data based on the paymentMethod
     *
     * @param paymentMethod the payment method
     * @return the specific Payment data
     */
    Object createPaymentInfoData(String paymentMethod);

    /**
     * Checks if the card info model has a payment token or not
     *
     * @param cartData the cart to check
     * @return boolean true if the token is missing, false otherwise
     */
    boolean isTokenMissingOnCardPaymentInfo(CartData cartData);

    /**
     * Updates payment info with checkout.com response details based on payment type
     *
     * @param paymentResponse checkout.com payment details response
     * @param paymentInfo     the payment info model to update
     */
    void updatePaymentInfoFromResponse(GetPaymentResponse paymentResponse, final PaymentInfoModel paymentInfo);

    /**
     * Processes the payment details response and perform the logic for each different payment method
     *
     * @param paymentResponse the payment details response from checkout.com
     */
    void processPaymentDetails(GetPaymentResponse paymentResponse);

}
