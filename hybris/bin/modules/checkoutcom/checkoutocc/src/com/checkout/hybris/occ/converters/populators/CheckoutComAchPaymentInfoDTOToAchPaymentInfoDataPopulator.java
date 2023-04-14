package com.checkout.hybris.occ.converters.populators;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the required attributes of the {@link AchPaymentInfoData}
 */
public class CheckoutComAchPaymentInfoDTOToAchPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, AchPaymentInfoData> {

    private static final String CORPORATE_ACCOUNT_TYPE_VALUE = "Corporate";
    private static final String CORP_SAVINGS_ACCOUNT_TYPE_VALUE = "CorpSavings";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO source, final AchPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDetailsWsDTO cannot be null.");
        Assert.notNull(target, "AchPaymentInfoData cannot be null.");

        target.setAccountHolderName(source.getAccountHolderName());
        target.setAccountNumber(source.getAccountNumber());
        target.setAccountType(source.getAccountType());
        target.setBankCode(source.getBankCode());
        target.setPaymentMethod(source.getPaymentMethod());
        target.setRoutingNumber(source.getRoutingNumber());
        if (target.getAccountType().equalsIgnoreCase(CORPORATE_ACCOUNT_TYPE_VALUE) || target.getAccountType().equalsIgnoreCase(CORP_SAVINGS_ACCOUNT_TYPE_VALUE)) {
            target.setCompanyName(source.getCompanyName());
        }
    }
}
