package com.checkout.hybris.addon.converters.impl;

import com.checkout.hybris.addon.converters.CheckoutComMappedPaymentDataFormReverseConverter;
import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Default implementation of the {@link CheckoutComMappedPaymentDataFormReverseConverter}
 */
public class DefaultCheckoutComMappedPaymentDataFormReverseConverter implements CheckoutComMappedPaymentDataFormReverseConverter {

    protected final Map<CheckoutComPaymentType, Converter<PaymentDataForm, Object>> convertersMap;
    protected final Converter<PaymentDataForm, Object> defaultConverter;

    public DefaultCheckoutComMappedPaymentDataFormReverseConverter(final Map<CheckoutComPaymentType, Converter<PaymentDataForm, Object>> convertersMap,
                                                                   final Converter<PaymentDataForm, Object> defaultConverter) {
        this.convertersMap = convertersMap;
        this.defaultConverter = defaultConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertPaymentDataForm(final PaymentDataForm paymentDataForm, final CheckoutComPaymentType paymentType) {
        Assert.notNull(paymentDataForm, "PaymentDataForm cannot be null.");
        Assert.notNull(paymentType, "PaymentType cannot be null.");

        if (convertersMap.containsKey(paymentType)) {
            return convertersMap.get(paymentType).convert(paymentDataForm);
        }
        return defaultConverter.convert(paymentDataForm);
    }
}