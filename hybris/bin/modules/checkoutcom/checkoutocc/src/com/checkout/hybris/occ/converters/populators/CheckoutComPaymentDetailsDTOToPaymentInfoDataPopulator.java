package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link APMPaymentInfoData}
 */
public class CheckoutComPaymentDetailsDTOToPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, APMPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final APMPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "Parameter PaymentDataForm cannot be null.");
        Assert.notNull(target, "Parameter APMPaymentInfoData cannot be null.");

        target.setType(source.getType());
    }
}
