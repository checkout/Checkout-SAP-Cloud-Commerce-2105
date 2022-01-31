package com.checkout.hybris.facades.payment.wallet;

import com.checkout.hybris.facades.beans.ApplePayPaymentRequestData;
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
}
