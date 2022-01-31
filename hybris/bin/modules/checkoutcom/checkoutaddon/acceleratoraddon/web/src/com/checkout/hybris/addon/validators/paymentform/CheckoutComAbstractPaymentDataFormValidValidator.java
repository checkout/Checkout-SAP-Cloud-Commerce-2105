package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.Validator;


/**
 * Abstract validator that implements the validators registration and exposes the get validator key method
 */
public abstract class CheckoutComAbstractPaymentDataFormValidValidator implements Validator {

    /**
     * Checks if the payment form contains the field and whether the value is blank or not
     *
     * @param paymentDataForm payment data form
     * @param fieldName       the field to validate
     * @return true if form doesn't contain the field or it's value is blank, false otherwise
     */
    protected boolean isFieldBlank(final PaymentDataForm paymentDataForm, final String fieldName) {
        return !paymentDataForm.getFormAttributes().containsKey(fieldName) ||
                StringUtils.isBlank((String) paymentDataForm.getFormAttributes().get(fieldName));
    }

}
