package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates Oxxo payment form
 */
public class CheckoutComOxxoPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

    private static final String DOCUMENT = "document";
    private static final String REGEX = "^[a-zA-Z0-9]{18}$";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class<?> clazz) {
        return PaymentDataForm.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object form, final Errors errors) {
        final PaymentDataForm paymentDataForm = (PaymentDataForm) form;
        final String document = (String) paymentDataForm.getFormAttributes().get(DOCUMENT);
        if (StringUtils.isBlank(document)) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "formAttributes['" + DOCUMENT + "']", "checkoutcom.oxxo.document.mandatory");
        } else if (!((String)paymentDataForm.getFormAttributes().get(DOCUMENT)).matches(REGEX)) {
            errors.rejectValue("formAttributes['" + DOCUMENT + "']", "checkoutcom.oxxo.document.invalid");
        }
    }
}
