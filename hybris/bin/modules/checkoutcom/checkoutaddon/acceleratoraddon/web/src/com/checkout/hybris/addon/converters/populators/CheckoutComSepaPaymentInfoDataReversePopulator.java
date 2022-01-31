package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.SepaPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link com.checkout.hybris.facades.beans.SepaPaymentInfoData}
 */
public class CheckoutComSepaPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, SepaPaymentInfoData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final SepaPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDataForm cannot be null.");
        Assert.notNull(target, "SepaPaymentInfoData cannot be null.");

        final Map<String, Object> formAttributes = source.getFormAttributes();

        target.setType(CheckoutComPaymentType.SEPA.name());

        target.setAccountIban((String) formAttributes.get("accountIban"));
        target.setPaymentType((String) formAttributes.get("paymentType"));
        target.setFirstName((String) formAttributes.get("firstName"));
        target.setLastName((String) formAttributes.get("lastName"));
        target.setAddressLine1((String) formAttributes.get("addressLine1"));
        target.setAddressLine2((String) formAttributes.get("addressLine2"));
        target.setPostalCode((String) formAttributes.get("postalCode"));
        target.setCity((String) formAttributes.get("city"));
        target.setCountry((String) formAttributes.get("country"));
    }
}