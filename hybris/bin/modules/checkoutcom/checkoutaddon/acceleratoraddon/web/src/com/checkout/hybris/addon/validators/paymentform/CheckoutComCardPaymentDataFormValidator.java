package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.google.common.collect.ImmutableList;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.List;

/**
 * Validates Card payment form
 */
public class CheckoutComCardPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

    /**
     * {@inheritDoc}
     */
    public boolean supports(final Class<?> clazz) {
        return PaymentDataForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object form, final Errors errors) {
        final PaymentDataForm paymentDataForm = (PaymentDataForm) form;
        final List<String> notEmptyFields = ImmutableList.of("paymentToken", "number", "cardBin", "validToMonth", "validToYear", "cardType");

        notEmptyFields.stream()
                .filter(field -> isFieldBlank(paymentDataForm, field))
                .forEach(field -> ValidationUtils.rejectIfEmptyOrWhitespace(errors, "formAttributes['" + field + "']", "checkoutcom.paymentdata.form.notempty.error"));
    }
}
