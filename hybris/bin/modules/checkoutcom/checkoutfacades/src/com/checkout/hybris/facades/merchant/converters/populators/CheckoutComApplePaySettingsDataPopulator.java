package com.checkout.hybris.facades.merchant.converters.populators;

import com.checkout.hybris.core.model.CheckoutComApplePayConfigurationModel;
import com.checkout.hybris.facades.beans.ApplePaySettingsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * Populates the ApplePaySettingsData from the CheckoutComApplePayConfigurationModel
 */
public class CheckoutComApplePaySettingsDataPopulator implements Populator<CheckoutComApplePayConfigurationModel, ApplePaySettingsData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CheckoutComApplePayConfigurationModel source, final ApplePaySettingsData target) throws ConversionException {
        Assert.notNull(source, "Source CheckoutComApplePayConfigurationModel cannot be null.");
        Assert.notNull(target, "Target ApplePaySettingsData cannot be null.");

        target.setMerchantId(source.getMerchantId());
        target.setMerchantName(source.getMerchantName());
        target.setCertificate(source.getCertificate());
        target.setPrivateKey(source.getPrivateKey());
        target.setCountryCode(source.getCountryCode());

        if (CollectionUtils.isNotEmpty(source.getMerchantCapabilities())) {
            final Set<String> merchantCapabilities = new HashSet<>();
            source.getMerchantCapabilities().forEach(merchantCap -> merchantCapabilities.add(merchantCap.getCode()));
            target.setMerchantCapabilities(merchantCapabilities);
        }

        if (CollectionUtils.isNotEmpty(source.getSupportedNetworks())) {
            final Set<String> supportedNetworks = new HashSet<>();
            source.getSupportedNetworks().forEach(supportedNet -> supportedNetworks.add(supportedNet.getCode()));
            target.setSupportedNetworks(supportedNetworks);
        }
    }
}