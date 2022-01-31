package com.checkout.hybris.facades.payment.converters;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.facades.beans.APMPaymentInfoData;

/**
 * Converts a payment info data into a specific apm payment info model based on the payment type
 */
public interface CheckoutComApmMappedPaymentInfoReverseConverter {

    /**
     * Converts the given apm payment info data into a specific payment info model based on the payment type.
     * If there is no converter for the payment type, will use the default converter
     *
     * @param apmPaymentInfoData the apm payment info data
     * @param paymentType        the payment type
     * @return {@link CheckoutComAPMPaymentInfoModel} for the paymentType
     */
    CheckoutComAPMPaymentInfoModel convertAPMPaymentInfoData(APMPaymentInfoData apmPaymentInfoData, CheckoutComPaymentType paymentType);
}
