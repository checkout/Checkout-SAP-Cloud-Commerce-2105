package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates Sepa paymentDetailsWsDTO
 */
public class CheckoutComSepaPaymentDetailsWsDTOValidator implements Validator {

    private static final String FIELD_REQUIRED_MESSAGE_ID = "field.required";

    /**
     * {@inheritDoc}
     */
    public boolean supports(final Class<?> clazz) {
        return PaymentDetailsWsDTO.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object form, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentType", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "accountIban", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "addressLine1", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "city", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "postalCode", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "country", FIELD_REQUIRED_MESSAGE_ID);
    }
}
