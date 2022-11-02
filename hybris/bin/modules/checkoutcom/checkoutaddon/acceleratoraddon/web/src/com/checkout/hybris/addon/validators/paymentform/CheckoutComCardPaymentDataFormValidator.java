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

    private static final String CARD_TYPE_KEY = "cardType";
    private static final String CARTES_BANCAIRES = "cartes_bancaires";
    private static final String CARTES_BANCAIRES_NOT_SUPPORTED_MSG = "checkoutcom.paymentdata.form.cartesbancaires.not.supported";

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
        final List<String> notEmptyFields = ImmutableList.of("paymentToken", "number", "cardBin", "validToMonth", "validToYear", CARD_TYPE_KEY);

        notEmptyFields.stream()
                .filter(field -> isFieldBlank(paymentDataForm, field))
                .forEach(field -> ValidationUtils.rejectIfEmptyOrWhitespace(errors, "formAttributes['" + field + "']", "checkoutcom.paymentdata.form.notempty.error"));

        final String cardType = (String) paymentDataForm.getFormAttributes().get(CARD_TYPE_KEY);
        if (CARTES_BANCAIRES.equals(cardType)) {
            errors.rejectValue("formAttributes['" + CARD_TYPE_KEY + "']", CARTES_BANCAIRES_NOT_SUPPORTED_MSG);
        }
    }
}
