package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates Klarna payment form
 */
public class CheckoutComKlarnaPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

    protected static final String AUTHORIZATION_TOKEN_KEY = "authorizationToken";

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
        if (isFieldBlank(paymentDataForm, AUTHORIZATION_TOKEN_KEY)) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "formAttributes['" + AUTHORIZATION_TOKEN_KEY + "']", "checkoutcom.klarna.authorization.token.mandatory");
        }
    }
}
