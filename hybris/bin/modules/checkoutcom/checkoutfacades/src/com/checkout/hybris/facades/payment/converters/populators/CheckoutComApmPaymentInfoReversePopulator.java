package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComAPMPaymentInfoModel}
 */
public class CheckoutComApmPaymentInfoReversePopulator implements Populator<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel> {

    protected final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService;

    public CheckoutComApmPaymentInfoReversePopulator(final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService) {
        this.checkoutComAPMConfigurationService = checkoutComAPMConfigurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final APMPaymentInfoData source, final CheckoutComAPMPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter APMPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComAPMPaymentInfoModel cannot be null.");

        target.setType(source.getType());
        target.setUserDataRequired(checkoutComAPMConfigurationService.isApmUserDataRequired(source.getType().toUpperCase()));
        target.setDeferred(true);
    }
}