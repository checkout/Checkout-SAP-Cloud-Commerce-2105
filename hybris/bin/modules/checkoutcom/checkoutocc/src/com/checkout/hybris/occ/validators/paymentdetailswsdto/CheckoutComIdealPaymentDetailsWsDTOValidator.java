package com.checkout.hybris.occ.validators.paymentdetailswsdto;

import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates Ideal paymentDetailsWsDTO
 */
public class CheckoutComIdealPaymentDetailsWsDTOValidator implements Validator {

    private static final String BIC_KEY = "bic";
    private static final String IDEAL_BIC_ERROR = "checkoutcom.occ.payment.ideal.bic.error";

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
        final String bic = paymentDetailsWsDTO.getBic();

        if (StringUtils.isBlank(bic) || !isValidBic(bic)) {
            errors.rejectValue(BIC_KEY, IDEAL_BIC_ERROR);
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
