package com.checkout.hybris.addon.converters;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;

/**
 * Converts a payment data form into a specific payment info data based on the payment type
 */
public interface CheckoutComMappedPaymentDataFormReverseConverter {

    /**
     * Converts the given payment data form into a specific payment data object based on the payment type.
     * If there is no converter for the payment type, will use the default converter
     *
     * @param paymentDataForm the payment data form
     * @param paymentType     the payment type
     * @return a specific payment info data for the type
     */
    Object convertPaymentDataForm(final PaymentDataForm paymentDataForm, final CheckoutComPaymentType paymentType);
}
