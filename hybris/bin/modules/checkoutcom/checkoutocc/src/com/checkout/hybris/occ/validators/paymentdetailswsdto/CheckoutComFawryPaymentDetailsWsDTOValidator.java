package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates Fawry paymentDetailsWsDTO
 */
public class CheckoutComFawryPaymentDetailsWsDTOValidator implements Validator {

    private static final String MOBILE_NUMBER_KEY = "mobileNumber";
    private static final String MOBILE_NUMBER_LENGTH = "checkoutcom.occ.fawry.mobile.number.length";
    private static final String MOBILE_NUMBER_MANDATORY = "checkoutcom.occ.fawry.mobile.number.mandatory";

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
        final String mobileNumber = paymentDetailsWsDTO.getMobileNumber();
        if (StringUtils.isBlank(mobileNumber)) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, MOBILE_NUMBER_KEY, MOBILE_NUMBER_MANDATORY);
        } else if (!isValidMobileNumber(mobileNumber)) {
            errors.rejectValue(MOBILE_NUMBER_KEY, MOBILE_NUMBER_LENGTH);
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
