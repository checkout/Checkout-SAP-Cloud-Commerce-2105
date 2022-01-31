package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.OxxoPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link OxxoPaymentInfoData}
 */
public class CheckoutComOxxoPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, OxxoPaymentInfoData> {

    protected static final String DOCUMENT_KEY = "document";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final OxxoPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDataForm cannot be null.");
        Assert.notNull(target, "OxxoPaymentInfoData cannot be null.");

        final Map<String, Object> formAttributes = source.getFormAttributes();

        target.setDocument((String) formAttributes.get(DOCUMENT_KEY));
        target.setType(CheckoutComPaymentType.OXXO.name());
    }
}
