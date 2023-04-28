package com.checkout.hybris.facades.payment.impl;

import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.core.payment.ach.service.CheckoutComACHConsentService;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.CheckoutComACHConsentFacade;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

/**
 * Default implementation of the {@link CheckoutComACHConsentFacade}
 */
public class DefaultCheckoutComACHConsentFacade implements CheckoutComACHConsentFacade {

    protected final CheckoutComACHConsentService checkoutComACHConsentService;

    protected final Converter<AchBankInfoDetailsData, CheckoutComACHConsentModel> checkoutComAchConsentReverseConverter;

    public DefaultCheckoutComACHConsentFacade(final CheckoutComACHConsentService checkoutComACHConsentService,
                                              final Converter<AchBankInfoDetailsData, CheckoutComACHConsentModel> checkoutComAchConsentReverseConverter) {
        this.checkoutComACHConsentService = checkoutComACHConsentService;
        this.checkoutComAchConsentReverseConverter = checkoutComAchConsentReverseConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createCheckoutComACHConsent(final AchBankInfoDetailsData achBankInfoDetailsData, final boolean customerConsents) throws CustomerConsentException {
        if (!customerConsents) {
            throw new CustomerConsentException();
        }

        final CheckoutComACHConsentModel checkoutComACHConsent = checkoutComAchConsentReverseConverter.convert(
                achBankInfoDetailsData);
        checkoutComACHConsentService.saveCheckoutComACHConsent(checkoutComACHConsent);
    }
}
