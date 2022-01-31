package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates Klarna paymentDetailsWsDTO
 */
public class CheckoutComKlarnaPaymentDetailsWsDTOValidator implements Validator {

    private static final String AUTHORIZATION_TOKEN_KEY = "authorizationToken";
    private static final String AUTHORIZATION_TOKEN_MANDATORY = "checkoutcom.occ.klarna.authorization.token.mandatory";

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
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, AUTHORIZATION_TOKEN_KEY, AUTHORIZATION_TOKEN_MANDATORY);
    }
}
