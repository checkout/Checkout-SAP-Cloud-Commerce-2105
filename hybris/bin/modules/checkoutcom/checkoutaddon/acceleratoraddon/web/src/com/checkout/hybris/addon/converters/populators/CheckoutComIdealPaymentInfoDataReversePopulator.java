package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.IdealPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link com.checkout.hybris.facades.beans.IdealPaymentInfoData}
 */
public class CheckoutComIdealPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, IdealPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final IdealPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDataForm cannot be null.");
        Assert.notNull(target, "IdealPaymentInfoData cannot be null.");

        final Map<String, Object> formAttributes = source.getFormAttributes();

        target.setType(CheckoutComPaymentType.IDEAL.name());
        target.setBic((String) formAttributes.get("bic"));
    }
}