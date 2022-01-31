package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComFawryPaymentInfoModel;
import com.checkout.hybris.facades.beans.FawryPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComFawryPaymentInfoModel}
 */
public class CheckoutComFawryPaymentInfoReversePopulator implements Populator<FawryPaymentInfoData, CheckoutComFawryPaymentInfoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final FawryPaymentInfoData source, final CheckoutComFawryPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter FawryPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComFawryPaymentInfoModel cannot be null.");

        target.setMobileNumber(source.getMobileNumber());
    }
}
