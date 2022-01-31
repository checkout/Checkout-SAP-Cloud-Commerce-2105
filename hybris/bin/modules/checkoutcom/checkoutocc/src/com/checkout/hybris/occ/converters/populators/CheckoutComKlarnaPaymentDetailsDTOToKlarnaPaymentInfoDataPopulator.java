package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.KlarnaPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the required attributes of the {@link KlarnaPaymentInfoData}
 */
public class CheckoutComKlarnaPaymentDetailsDTOToKlarnaPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, KlarnaPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final KlarnaPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDetailsWsDTO cannot be null.");
        Assert.notNull(target, "KlarnaPaymentInfoData cannot be null.");

        target.setAuthorizationToken(source.getAuthorizationToken());
        target.setType(CheckoutComPaymentType.KLARNA.name());
    }
}
