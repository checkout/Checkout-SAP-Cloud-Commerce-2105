package com.checkout.hybris.facades.merchant.impl;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComGooglePayConfigurationModel;
import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

/**
 * Default implementation of the {@link CheckoutComMerchantConfigurationFacade}
 */
public class DefaultCheckoutComMerchantConfigurationFacade implements CheckoutComMerchantConfigurationFacade {

    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final Converter<CheckoutComApplePayConfigurationModel, ApplePaySettingsData> checkoutComApplePaySettingsDataConverter;
    protected final Converter<CheckoutComGooglePayConfigurationModel, GooglePaySettingsData> checkoutComGooglePaySettingsDataConverter;

    public DefaultCheckoutComMerchantConfigurationFacade(final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                         final Converter<CheckoutComApplePayConfigurationModel, ApplePaySettingsData> checkoutComApplePaySettingsDataConverter,
                                                         final Converter<CheckoutComGooglePayConfigurationModel, GooglePaySettingsData> checkoutComGooglePaySettingsDataConverter) {
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.checkoutComApplePaySettingsDataConverter = checkoutComApplePaySettingsDataConverter;
        this.checkoutComGooglePaySettingsDataConverter = checkoutComGooglePaySettingsDataConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCheckoutComMerchantPublicKey() {
        return checkoutComMerchantConfigurationService.getPublicKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ApplePaySettingsData> getApplePaySettings() {
        final CheckoutComApplePayConfigurationModel applePayConfiguration = checkoutComMerchantConfigurationService.getApplePayConfiguration();
        if (applePayConfiguration != null) {
            return Optional.ofNullable(checkoutComApplePaySettingsDataConverter.convert(applePayConfiguration));
        } else {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GooglePaySettingsData> getGooglePaySettings() {
        final CheckoutComGooglePayConfigurationModel googlePayConfiguration = checkoutComMerchantConfigurationService.getGooglePayConfiguration();
        if (googlePayConfiguration != null) {
            return Optional.ofNullable(checkoutComGooglePaySettingsDataConverter.convert(googlePayConfiguration));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Boolean isCheckoutComMerchantABC() {
        boolean isNasUsed = checkoutComMerchantConfigurationService.isNasUsed();
        return (isNasUsed ? Boolean.FALSE : Boolean.TRUE);
    }
}
