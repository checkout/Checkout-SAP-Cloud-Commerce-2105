package com.checkout.hybris.facades.merchant;

import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;

import java.util.Optional;

/**
 * Facade to handle datas about CheckoutComMerchantConfiguration
 */
public interface CheckoutComMerchantConfigurationFacade {

    /**
     * Returns the public key of the CheckoutComMerchantConfiguration for the current base site
     *
     * @return String the key
     */
    String getCheckoutComMerchantPublicKey();

    /**
     * Returns the {@link ApplePaySettingsData} of the CheckoutComMerchantConfiguration for the current base site
     *
     * @return the optional with apple pay settings if defined, optional empty otherwise
     */
    Optional<ApplePaySettingsData> getApplePaySettings();

    /**
     * Returns the {@link GooglePaySettingsData} of the CheckoutComMerchantConfiguration for the current base site
     *
     * @return the optional with google pay settings if defined, optional empty otherwise
     */
    Optional<GooglePaySettingsData> getGooglePaySettings();

    /**
     * Returns true if the merchant is configured as ABC and false if it's NAS.
     * 
     * @return  {@link Boolean} the value.
     * 
     */
    Boolean isCheckoutComMerchantABC ();
}
