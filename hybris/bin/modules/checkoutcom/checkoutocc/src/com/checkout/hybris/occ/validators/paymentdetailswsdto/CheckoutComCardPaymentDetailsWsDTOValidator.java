package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.CardTypeWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Optional;

/**
 * Validates Card Payment Details WS DTO
 */
public class CheckoutComCardPaymentDetailsWsDTOValidator implements Validator {

    private static final String FIELD_REQUIRED_MESSAGE_ID = "field.required";

    private static final String CARD_TYPE_CODE_KEY = "cardType.code";
    private static final String CARTES_BANCAIRES = "cartes_bancaires";
    private static final String CARTES_BANCAIRES_NOT_SUPPORTED_MSG = "checkoutcom.paymentdata.form.cartesbancaires.not.supported";

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
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, CARD_TYPE_CODE_KEY, FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardNumber", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expiryMonth", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "expiryYear", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentToken", FIELD_REQUIRED_MESSAGE_ID);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "cardBin", FIELD_REQUIRED_MESSAGE_ID);

        final String cardType = Optional.ofNullable(((PaymentDetailsWsDTO) target).getCardType())
                .map(CardTypeWsDTO::getCode)
                .orElse("");
        if (CARTES_BANCAIRES.equals(cardType)) {
            errors.rejectValue(CARD_TYPE_CODE_KEY, CARTES_BANCAIRES_NOT_SUPPORTED_MSG);
        }
    }
}
