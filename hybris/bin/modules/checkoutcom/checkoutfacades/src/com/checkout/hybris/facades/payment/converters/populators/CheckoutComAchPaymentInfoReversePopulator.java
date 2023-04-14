package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.enums.AchAccountType;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComAchPaymentInfoModel}
 */
public class CheckoutComAchPaymentInfoReversePopulator implements Populator<AchPaymentInfoData, CheckoutComAchPaymentInfoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final AchPaymentInfoData source, final CheckoutComAchPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter AchPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComAchPaymentInfoModel cannot be null.");

        target.setAccountHolderName(source.getAccountHolderName());
        target.setAccountNumber(source.getAccountNumber());
        target.setAccountType(StringUtils.isNotBlank(source.getAccountType()) ? AchAccountType.valueOf(source.getAccountType()) : null);
        target.setPaymentMethod(source.getPaymentMethod());
        target.setBankCode(source.getBankCode());
        target.setRoutingNumber(source.getRoutingNumber());
        target.setType(CheckoutComPaymentType.ACH.name());
        target.setCompanyName(source.getCompanyName());
        target.setMask(source.getMask());
    }
}
