package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.List;

/**
 * Validates Sepa apm payment form
 */
public class CheckoutComSepaPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

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
        final List<String> notEmptyFields = List.of("firstName", "lastName", "paymentType", "accountIban", "addressLine1", "city", "postalCode", "country");

        notEmptyFields.stream()
                .filter(field -> isFieldBlank(paymentDataForm, field))
                .forEach(field -> ValidationUtils.rejectIfEmptyOrWhitespace(errors, "formAttributes['" + field + "']", "checkoutcom.paymentdata.form.notempty.error"));
    }
}
