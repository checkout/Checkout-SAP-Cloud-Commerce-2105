package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.IdealPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the required attributes of the {@link IdealPaymentInfoData}
 */
public class CheckoutComIdealPaymentDetailsDTOToIdealPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, IdealPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final IdealPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDetailsWsDTO cannot be null.");
        Assert.notNull(target, "IdealPaymentInfoData cannot be null.");

        target.setType(CheckoutComPaymentType.IDEAL.name());
        target.setBic(source.getBic());
    }
}
