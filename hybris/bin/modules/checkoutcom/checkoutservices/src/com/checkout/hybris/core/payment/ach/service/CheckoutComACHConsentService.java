package com.checkout.hybris.core.payment.ach.service;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;

/**
 * Interface that operates with ACH consents
 */
public interface CheckoutComACHConsentService {

    /**
     * Creates an ACH Consent and saves it
     *
     * @param checkoutComACHConsent the ACH consent to be saved
     */
    void saveCheckoutComACHConsent(CheckoutComACHConsentModel checkoutComACHConsent);
}
