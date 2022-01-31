package com.checkout.hybris.facades.merchant.converters.populators;

import com.checkout.hybris.core.model.CheckoutComGooglePayConfigurationModel;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

/**
 * Populates the GooglePaySettingsData from the CheckoutComGooglePayConfigurationModel
 */
public class CheckoutComGooglePaySettingsDataPopulator implements Populator<CheckoutComGooglePayConfigurationModel, GooglePaySettingsData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CheckoutComGooglePayConfigurationModel source, final GooglePaySettingsData target) throws ConversionException {
        Assert.notNull(source, "Source CheckoutComGooglePayConfigurationModel cannot be null.");
        Assert.notNull(target, "Target GooglePaySettingsData cannot be null.");

        target.setMerchantId(source.getMerchantId());
        target.setMerchantName(source.getMerchantName());
        target.setEnvironment(source.getEnvironment().getCode().toUpperCase());
        target.setGateway(source.getGateway());
        target.setGatewayMerchantId(source.getGatewayMerchantId());
        target.setType(source.getType());

        if (CollectionUtils.isNotEmpty(source.getAllowedCardNetworks())) {
            final Set<String> allowedCardNetworks = new HashSet<>();
            source.getAllowedCardNetworks().forEach(cardNetwork -> allowedCardNetworks.add(cardNetwork.getCode()));
            target.setAllowedCardNetworks(allowedCardNetworks);
        }

        if (CollectionUtils.isNotEmpty(source.getAllowedCardAuthMethods())) {
            final Set<String> allowedCardAuthMethods = new HashSet<>();
            source.getAllowedCardAuthMethods().forEach(cardAuthMethod -> allowedCardAuthMethods.add(cardAuthMethod.getCode()));
            target.setAllowedAuthMethods(allowedCardAuthMethods);
        }
    }
}