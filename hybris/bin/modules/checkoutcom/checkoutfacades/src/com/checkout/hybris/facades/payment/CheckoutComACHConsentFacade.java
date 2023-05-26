package com.checkout.hybris.facades.payment;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;

/**
 * Handles ACH Consent facade logic
 */
public interface CheckoutComACHConsentFacade {

    /**
     * Creates an ACH Consent and saves it
     *
     * @param achBankInfoDetailsData the ach bank info details
     */
    void createCheckoutComACHConsent(AchBankInfoDetailsData achBankInfoDetailsData, boolean customerConsents) throws CustomerConsentException;

}
