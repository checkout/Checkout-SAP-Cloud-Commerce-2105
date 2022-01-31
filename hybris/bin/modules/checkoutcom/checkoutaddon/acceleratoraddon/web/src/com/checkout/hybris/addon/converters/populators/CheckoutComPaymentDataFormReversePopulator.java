package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

/**
 * Populates the custom properties of the extended {@link APMPaymentInfoData}
 */
public class CheckoutComPaymentDataFormReversePopulator implements Populator<PaymentDataForm, APMPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final APMPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "Parameter PaymentDataForm cannot be null.");
        Assert.notNull(target, "Parameter APMPaymentInfoData cannot be null.");

        target.setType((String) source.getFormAttributes().get("type"));
    }
}
