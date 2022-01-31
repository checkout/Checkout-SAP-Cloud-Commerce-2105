package com.checkout.hybris.facades.payment.converters.populators;


import com.checkout.hybris.core.model.CheckoutComOxxoPaymentInfoModel;
import com.checkout.hybris.facades.beans.OxxoPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComOxxoPaymentInfoModel}
 */
public class CheckoutComOxxoPaymentInfoReversePopulator implements Populator<OxxoPaymentInfoData, CheckoutComOxxoPaymentInfoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OxxoPaymentInfoData source, final CheckoutComOxxoPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter OxxoPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComOxxoPaymentInfoModel cannot be null.");

        target.setDocument(source.getDocument());
    }
}
