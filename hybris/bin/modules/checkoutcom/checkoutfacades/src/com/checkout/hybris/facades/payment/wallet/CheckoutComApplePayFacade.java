package com.checkout.hybris.facades.payment.wallet;

import com.checkout.hybris.facades.beans.ApplePayPaymentRequestData;
import com.checkout.hybris.facades.beans.ApplePayShippingContactUpdate;
import com.checkout.hybris.facades.beans.ApplePayShippingMethodUpdate;
import com.checkout.hybris.facades.beans.ApplePayValidateMerchantRequestData;

/**
 * Facade for apple pay functionalities
 */
public interface CheckoutComApplePayFacade {

    /**
     * Validates the session for apple pay
     *
     * @param validateMerchantRequestData the validate session request
     * @return the response
     */
    Object requestApplePayPaymentSession(ApplePayValidateMerchantRequestData validateMerchantRequestData);

    /**
     * Create a basic payment request for the session cart that can be used to initialize the ApplePaySession in the browser
     */
    ApplePayPaymentRequestData getApplePayPaymentRequest();

    /**
     * Creates an object containing the updated info after the user has selected or filled the delivery address on the apple pay pop-up
     * @return An object containing the updated info
     */
    ApplePayShippingContactUpdate getApplePayShippingContactUpdate();

    /**
     * Creates an object containing the updated info after the user has selected or filled the delivery method on the apple pay pop-up
     * @return An object containing the updated info
     */
    ApplePayShippingMethodUpdate getApplePayShippingMethodUpdate();
}
