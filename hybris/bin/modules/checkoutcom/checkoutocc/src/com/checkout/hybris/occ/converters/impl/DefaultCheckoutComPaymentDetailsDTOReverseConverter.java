package com.checkout.hybris.occ.converters.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.occ.converters.CheckoutComPaymentDetailsDTOReverseConverter;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Default implementation of the {@link CheckoutComPaymentDetailsDTOReverseConverter}
 */
public class DefaultCheckoutComPaymentDetailsDTOReverseConverter implements CheckoutComPaymentDetailsDTOReverseConverter {

    protected final Map<CheckoutComPaymentType, Converter<PaymentDetailsWsDTO, Object>> converters;
    protected final Converter<PaymentDetailsWsDTO, Object> defaultConverter;

    public DefaultCheckoutComPaymentDetailsDTOReverseConverter(final Map<CheckoutComPaymentType, Converter<PaymentDetailsWsDTO, Object>> converters,
                                                        final Converter<PaymentDetailsWsDTO, Object> defaultConverter) {
        this.converters = converters;
        this.defaultConverter = defaultConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convertPaymentDetailsWsDTO(final PaymentDetailsWsDTO paymentDetailsWsDTO, final CheckoutComPaymentType paymentType) {
        Assert.notNull(paymentDetailsWsDTO, "PaymentDataForm cannot be null.");
        Assert.notNull(paymentType, "PaymentType cannot be null.");

        return converters.getOrDefault(paymentType, defaultConverter).convert(paymentDetailsWsDTO);
    }
}
