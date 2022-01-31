package com.checkout.hybris.addon.validators.paymentform;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * Validates Fawry payment form
 */
public class CheckoutComFawryPaymentDataFormValidator extends CheckoutComAbstractPaymentDataFormValidValidator {

    private static final String MOBILE_NUMBER_KEY = "mobileNumber";

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
        final String mobileNumber = (String) paymentDataForm.getFormAttributes().get(MOBILE_NUMBER_KEY);
        if (isFieldBlank(paymentDataForm, MOBILE_NUMBER_KEY)) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "formAttributes['" + MOBILE_NUMBER_KEY + "']", "checkoutcom.fawry.mobile.number.mandatory");
        } else if (!isValidMobileNumber(mobileNumber)) {
            errors.rejectValue("formAttributes['" + MOBILE_NUMBER_KEY + "']", "checkoutcom.fawry.mobile.number.length");
        }
    }

    /**
     * Checks if the mobile number has a valid length
     *
     * @param mobileNumber the mobile number string
     * @return true if valid, false otherwise
     */
    protected boolean isValidMobileNumber(final String mobileNumber) {
        return mobileNumber.matches("\\d{11}");
    }
}
