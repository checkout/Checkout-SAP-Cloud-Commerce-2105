package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.FawryPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the required attributes of the {@link FawryPaymentInfoData}
 */
public class CheckoutComFawryPaymentDetailsDTOToFawryPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, FawryPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final FawryPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDetailsWsDTO cannot be null.");
        Assert.notNull(target, "FawryPaymentInfoData cannot be null.");

        target.setMobileNumber(source.getMobileNumber());
        target.setType(CheckoutComPaymentType.FAWRY.name());
    }
}
