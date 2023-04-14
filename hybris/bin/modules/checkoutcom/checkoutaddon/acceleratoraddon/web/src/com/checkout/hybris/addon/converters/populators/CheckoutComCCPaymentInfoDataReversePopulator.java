package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link CCPaymentInfoData}
 */
public class CheckoutComCCPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, CCPaymentInfoData> {

    protected static final String SAVE_CARD_KEY = "saveCard";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm paymentTokenForm, final CCPaymentInfoData ccPaymentInfoData) throws ConversionException {
        Assert.notNull(ccPaymentInfoData, "Parameter ccPaymentInfoData cannot be null.");
        Assert.notNull(paymentTokenForm, "Parameter paymentTokenForm cannot be null.");

        final Map<String, Object> formAttributes = paymentTokenForm.getFormAttributes();

        ccPaymentInfoData.setCardNumber((String) formAttributes.get("number"));
        ccPaymentInfoData.setCardType(StringUtils.deleteWhitespace((String) formAttributes.get("cardType")));
        ccPaymentInfoData.setExpiryMonth((String) formAttributes.get("validToMonth"));
        ccPaymentInfoData.setExpiryYear((String) formAttributes.get("validToYear"));
        ccPaymentInfoData.setPaymentToken((String) formAttributes.get("paymentToken"));
        ccPaymentInfoData.setSaved(formAttributes.containsKey(SAVE_CARD_KEY) && Boolean.parseBoolean((String) formAttributes.get(SAVE_CARD_KEY)));
        ccPaymentInfoData.setCardBin((String) formAttributes.get("cardBin"));
        ccPaymentInfoData.setScheme((String) formAttributes.get("schemeLocal"));
        ccPaymentInfoData.setAccountHolderName((String) formAttributes.get("accountHolderName"));
    }
}
