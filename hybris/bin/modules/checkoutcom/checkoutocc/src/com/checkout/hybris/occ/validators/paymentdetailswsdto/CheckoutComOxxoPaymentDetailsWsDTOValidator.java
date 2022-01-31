package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates Oxxo paymentDetailsWsDTO
 */
public class CheckoutComOxxoPaymentDetailsWsDTOValidator implements Validator {

    private static final String DOCUMENT_FIELD = "document";
    private static final String DOCUMENT_MANDATORY_MESSAGE = "checkoutcom.occ.oxxo.document.mandatory";
    private static final String REGEX = "^[a-zA-Z0-9]{18}$";
    private static final String DOCUMENT_INVALID_MESSAGE = "checkoutcom.occ.oxxo.document.invalid";

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
        final PaymentDetailsWsDTO paymentDetailsWsDTO = (PaymentDetailsWsDTO) form;

        if (StringUtils.isBlank(paymentDetailsWsDTO.getDocument())) {
            errors.rejectValue(DOCUMENT_FIELD, DOCUMENT_MANDATORY_MESSAGE);
        } else if (!paymentDetailsWsDTO.getDocument().matches(REGEX))
            errors.rejectValue(DOCUMENT_FIELD, DOCUMENT_INVALID_MESSAGE);
    }
}
