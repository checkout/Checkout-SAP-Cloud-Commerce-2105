package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates Card Payment Details WS DTO
 */
public class CheckoutComCardPaymentDetailsWsDTOValidator implements Validator {

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
    public void validate(final Object target, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardType.code", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardNumber", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expiryMonth", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expiryYear", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentToken", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardBin", FIELD_REQUIRED_MESSAGE_ID);
    }
}
