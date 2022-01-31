package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.KlarnaPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link KlarnaPaymentInfoData}
 */
public class CheckoutComKlarnaPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, KlarnaPaymentInfoData> {

    protected static final String AUTHORIZATION_TOKEN_KEY = "authorizationToken";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final KlarnaPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDataForm cannot be null.");
        Assert.notNull(target, "KlarnaPaymentInfoData cannot be null.");

        final Map<String, Object> formAttributes = source.getFormAttributes();

        target.setAuthorizationToken((String) formAttributes.get(AUTHORIZATION_TOKEN_KEY));
        target.setType(CheckoutComPaymentType.KLARNA.name());
    }
}
