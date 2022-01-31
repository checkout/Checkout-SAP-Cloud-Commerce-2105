package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComIdealPaymentInfoModel;
import com.checkout.hybris.facades.beans.IdealPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComIdealPaymentInfoModel}
 */
public class CheckoutComIdealPaymentInfoReversePopulator implements Populator<IdealPaymentInfoData, CheckoutComIdealPaymentInfoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final IdealPaymentInfoData source, final CheckoutComIdealPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter IdealPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComIdealPaymentInfoModel cannot be null.");

        target.setBic(source.getBic());
    }
}