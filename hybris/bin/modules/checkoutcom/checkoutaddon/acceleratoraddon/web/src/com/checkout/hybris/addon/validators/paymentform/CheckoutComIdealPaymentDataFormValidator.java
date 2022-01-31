package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import org.springframework.validation.Errors;

/**
 * Validates Ideal payment form
 */
public class CheckoutComIdealPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

    protected static final String BIC_KEY = "bic";

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
        final String bic = (String) paymentDataForm.getFormAttributes().get(BIC_KEY);

        if (isFieldBlank(paymentDataForm, BIC_KEY) || !isValidBic(bic)) {
            errors.rejectValue("formAttributes['" + BIC_KEY + "']", "checkoutcom.payment.ideal.bic.error");
        }
    }

    /**
     * Checks if bic field has the correct length
     *
     * @param bic the bic (iban) value
     * @return true if valid, false otherwise
     */
    protected boolean isValidBic(final String bic) {
        return bic.matches("^[a-zA-Z0-9]{8}$|^[a-zA-Z0-9]{11}$");
    }
}
