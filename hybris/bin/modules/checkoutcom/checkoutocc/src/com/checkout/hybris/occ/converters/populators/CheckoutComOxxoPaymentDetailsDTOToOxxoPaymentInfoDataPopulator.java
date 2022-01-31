package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.OxxoPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the required attributes of the {@link OxxoPaymentInfoData}
 */
public class CheckoutComOxxoPaymentDetailsDTOToOxxoPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, OxxoPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final OxxoPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDetailsWsDTO cannot be null.");
        Assert.notNull(target, "OxxoPaymentInfoData cannot be null.");

        target.setDocument(source.getDocument());
        target.setType(CheckoutComPaymentType.OXXO.name());
    }
}
