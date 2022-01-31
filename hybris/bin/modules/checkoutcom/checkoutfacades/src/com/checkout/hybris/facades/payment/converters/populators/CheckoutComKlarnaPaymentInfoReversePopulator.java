package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComKlarnaAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.KlarnaPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link CheckoutComKlarnaAPMPaymentInfoModel}
 */
public class CheckoutComKlarnaPaymentInfoReversePopulator implements Populator<KlarnaPaymentInfoData, CheckoutComKlarnaAPMPaymentInfoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final KlarnaPaymentInfoData source, final CheckoutComKlarnaAPMPaymentInfoModel target) throws ConversionException {
        Assert.notNull(source, "Parameter KlarnaPaymentInfoData  cannot be null.");
        Assert.notNull(target, "Parameter CheckoutComKlarnaAPMPaymentInfoModel cannot be null.");

        target.setAuthorizationToken(source.getAuthorizationToken());
        target.setDeferred(false);
    }
}