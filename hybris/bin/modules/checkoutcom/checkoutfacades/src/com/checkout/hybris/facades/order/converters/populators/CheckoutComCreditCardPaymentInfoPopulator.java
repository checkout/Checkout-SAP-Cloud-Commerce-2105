package com.checkout.hybris.facades.order.converters.populators;

import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates from CheckoutComCreditCardPaymentInfoModel to CCPaymentInfoData
 */
public class CheckoutComCreditCardPaymentInfoPopulator implements Populator<CheckoutComCreditCardPaymentInfoModel, CCPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfo, final CCPaymentInfoData ccPaymentInfoData) throws ConversionException {
        Assert.notNull(checkoutComCreditCardPaymentInfo, "Parameter checkoutComCreditCardPaymentInfo cannot be null.");
        Assert.notNull(ccPaymentInfoData, "Parameter ccPaymentInfoData cannot be null.");

        ccPaymentInfoData.setPaymentToken(checkoutComCreditCardPaymentInfo.getCardToken());
    }
}
