package com.checkout.hybris.facades.payment.google;

import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;

/**
 * Facade for Google Pay functionalities.
 */
public interface CheckoutComGooglePayFacade {

    /**
     * Create a Google payment request for the session cart.
     *
     * @return a Google Pay payment merchant configuration.
     */
    GooglePayMerchantConfigurationData getGooglePayMerchantConfiguration();
}
