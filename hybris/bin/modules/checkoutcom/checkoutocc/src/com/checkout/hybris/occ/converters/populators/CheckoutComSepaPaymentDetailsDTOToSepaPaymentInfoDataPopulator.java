package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.SepaPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the required attributes of the {@link SepaPaymentInfoData}
 */
public class CheckoutComSepaPaymentDetailsDTOToSepaPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, SepaPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final SepaPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDetailsWsDTO cannot be null.");
        Assert.notNull(target, "SepaPaymentInfoData cannot be null.");

        target.setType(CheckoutComPaymentType.SEPA.name());
        target.setAccountIban(source.getAccountIban());
        target.setPaymentType(source.getPaymentType());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAddressLine1(source.getAddressLine1());
        target.setAddressLine2(source.getAddressLine2());
        target.setPostalCode(source.getPostalCode());
        target.setCity(source.getCity());
        target.setCountry(source.getCountry());
    }
}
