package com.checkout.hybris.occ.converters.populators;

import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Populates the required attributes of the {@link CCPaymentInfoData}
 */
public class CheckoutComCCPaymentDetailsDTOToPaymentInfoDataPopulator implements Populator<PaymentDetailsWsDTO, CCPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDetailsWsDTO paymentDetailsWsDTO, final CCPaymentInfoData ccPaymentInfoData) throws ConversionException {
        Assert.notNull(ccPaymentInfoData, "Parameter ccPaymentInfoData cannot be null.");
        Assert.notNull(paymentDetailsWsDTO, "Parameter paymentDetailsWsDTO cannot be null.");

        ccPaymentInfoData.setCardNumber(paymentDetailsWsDTO.getCardNumber());
        ccPaymentInfoData.setCardType(StringUtils.deleteWhitespace(paymentDetailsWsDTO.getCardType().getCode()));
        ccPaymentInfoData.setExpiryMonth(paymentDetailsWsDTO.getExpiryMonth());
        ccPaymentInfoData.setExpiryYear(paymentDetailsWsDTO.getExpiryYear());
        ccPaymentInfoData.setPaymentToken(paymentDetailsWsDTO.getPaymentToken());
        ccPaymentInfoData.setScheme(paymentDetailsWsDTO.getScheme());
        ccPaymentInfoData.setCardBin(paymentDetailsWsDTO.getCardBin());
        ccPaymentInfoData.setSaved(Optional.ofNullable(paymentDetailsWsDTO.getSaved()).orElse(Boolean.FALSE));
        ccPaymentInfoData.setAccountHolderName(paymentDetailsWsDTO.getAccountHolderName());
    }
}
