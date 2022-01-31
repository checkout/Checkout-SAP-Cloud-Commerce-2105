package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.enums.SepaPaymentType;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.facades.beans.SepaPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComSepaPaymentInfoModel}
 */
public class CheckoutComSepaPaymentInfoReversePopulator implements Populator<SepaPaymentInfoData, CheckoutComSepaPaymentInfoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final SepaPaymentInfoData source, final CheckoutComSepaPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter SepaPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComSepaPaymentInfoModel cannot be null.");

        target.setAccountIban(source.getAccountIban());
        target.setPaymentType(StringUtils.isNotBlank(source.getPaymentType()) ? SepaPaymentType.valueOf(source.getPaymentType()) : null);
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setAddressLine1(source.getAddressLine1());
        target.setAddressLine2(source.getAddressLine2());
        target.setPostalCode(source.getPostalCode());
        target.setCity(source.getCity());
        target.setCountry(source.getCountry());
    }
}