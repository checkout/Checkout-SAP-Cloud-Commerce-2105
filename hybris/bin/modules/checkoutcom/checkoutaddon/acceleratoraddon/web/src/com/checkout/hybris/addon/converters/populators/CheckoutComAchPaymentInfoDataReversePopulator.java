package com.checkout.hybris.addon.converters.populators;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.facades.beans.AchPaymentInfoData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Populates the required attributes of the {@link AchPaymentInfoData}
 */
public class CheckoutComAchPaymentInfoDataReversePopulator implements Populator<PaymentDataForm, AchPaymentInfoData> {

    protected static final String ACCOUNT_HOLDER_NAME = "accountHolderName";
    protected static final String ACCOUNT_NUMBER = "accountNumber";
    protected static final String ACCOUNT_TYPE_KEY = "accountType";
    protected static final String ROUTING_NUMBER_KEY = "routingNumber";
    protected static final String COMPANY_NAME_KEY = "companyName";
    protected static final String CORPORATE_ACCOUNT_TYPE_VALUE = "Corporate";
    protected static final String CORP_SAVINGS_ACCOUNT_TYPE_VALUE = "CorpSavings";
    protected static final String PAYMENT_METHOD_KEY = "paymentMethod";
    protected static final String BANK_CODE_KEY = "bankCode";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final PaymentDataForm source, final AchPaymentInfoData target) throws ConversionException {
        Assert.notNull(source, "PaymentDataForm cannot be null.");
        Assert.notNull(target, "AchPaymentInfoData cannot be null.");

        final Map<String, Object> formAttributes = source.getFormAttributes();

        target.setAccountHolderName((String) formAttributes.get(ACCOUNT_HOLDER_NAME));
        target.setAccountNumber((String) formAttributes.get(ACCOUNT_NUMBER));
        target.setAccountType((String) formAttributes.get(ACCOUNT_TYPE_KEY));
        target.setRoutingNumber((String) formAttributes.get(ROUTING_NUMBER_KEY));
        if (target.getAccountType().equalsIgnoreCase(CORPORATE_ACCOUNT_TYPE_VALUE) || target.getAccountType().equalsIgnoreCase(CORP_SAVINGS_ACCOUNT_TYPE_VALUE)) {
            target.setCompanyName((String) formAttributes.get(COMPANY_NAME_KEY));
        }
        target.setPaymentMethod((String) formAttributes.get(PAYMENT_METHOD_KEY));
        target.setBankCode((String) formAttributes.get(BANK_CODE_KEY));

    }
}
