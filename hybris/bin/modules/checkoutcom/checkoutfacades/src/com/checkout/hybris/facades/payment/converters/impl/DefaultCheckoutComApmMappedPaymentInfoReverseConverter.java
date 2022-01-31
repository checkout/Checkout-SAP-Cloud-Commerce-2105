package com.checkout.hybris.facades.payment.converters.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;
import com.checkout.hybris.facades.payment.converters.CheckoutComApmMappedPaymentInfoReverseConverter;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Default implementation of the {@link CheckoutComApmMappedPaymentInfoReverseConverter}
 */
public class DefaultCheckoutComApmMappedPaymentInfoReverseConverter implements CheckoutComApmMappedPaymentInfoReverseConverter {

    protected final Map<CheckoutComPaymentType, Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel>> convertersMap;
    protected final Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel> defaultConverter;

    public DefaultCheckoutComApmMappedPaymentInfoReverseConverter(final Map<CheckoutComPaymentType, Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel>> convertersMap,
                                                                  final Converter<APMPaymentInfoData, CheckoutComAPMPaymentInfoModel> defaultConverter) {
        this.convertersMap = convertersMap;
        this.defaultConverter = defaultConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComAPMPaymentInfoModel convertAPMPaymentInfoData(final APMPaymentInfoData apmPaymentInfoData, final CheckoutComPaymentType paymentType) {
        Assert.notNull(apmPaymentInfoData, "APMPaymentInfoData cannot be null.");
        Assert.notNull(paymentType, "PaymentType cannot be null.");

        if (convertersMap.containsKey(paymentType)) {
            return convertersMap.get(paymentType).convert(apmPaymentInfoData);
        }
        return defaultConverter.convert(apmPaymentInfoData);
    }
}