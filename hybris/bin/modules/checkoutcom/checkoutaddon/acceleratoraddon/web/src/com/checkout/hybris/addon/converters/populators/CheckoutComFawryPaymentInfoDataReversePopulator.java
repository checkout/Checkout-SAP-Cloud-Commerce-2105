package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.FawryPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link FawryPaymentInfoData}
 */
public class CheckoutComFawryPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, FawryPaymentInfoData> {

    protected static final String MOBILE_NUMBER_KEY = "mobileNumber";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final FawryPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDataForm cannot be null.");
        Assert.notNull(target, "FawryPaymentInfoData cannot be null.");

        final Map<String, Object> formAttributes = source.getFormAttributes();

        target.setMobileNumber((String) formAttributes.get(MOBILE_NUMBER_KEY));
        target.setType(CheckoutComPaymentType.FAWRY.name());
    }
}
