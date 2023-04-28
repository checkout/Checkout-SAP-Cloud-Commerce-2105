package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.enums.AchAccountType;
import com.checkout.hybris.core.model.CheckoutComACHConsentModel;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComACHConsentModel}
 */
public class CheckoutComAchConsentReversePopulator implements Populator<AchBankInfoDetailsData, CheckoutComACHConsentModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final AchBankInfoDetailsData source, final CheckoutComACHConsentModel target) throws ConversionException {
        Assert.notNull(source, "Parameter AchPaymentInfoData cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComACHConsentModel cannot be null.");

        target.setPayer(source.getAccountHolderName());
        target.setEmail(source.getAccountHolderEmail());
        target.setAccountNumber(source.getAccountNumber());
        target.setAccountType(StringUtils.isNotBlank(source.getAccountType()) ? AchAccountType.valueOf(source.getAccountType()) : null);
        target.setBankName(source.getInstitutionName());
        target.setRoutingNumber(source.getBankRouting());
    }
}
