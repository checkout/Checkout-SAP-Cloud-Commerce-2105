package com.checkout.hybris.occ.validators.impl;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Validates the PaymentDetailsWsDTO and calls a specific validator based on the payment method
 */
public class CheckoutComPaymentDetailsWsDTOValidValidator implements Validator {

    protected static final String TYPE = "type";

    protected final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    protected final Map<CheckoutComPaymentType, Validator> validators;

    public CheckoutComPaymentDetailsWsDTOValidValidator(final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver,
                                                        final Map<CheckoutComPaymentType, Validator> validators) {
        this.checkoutComPaymentTypeResolver = checkoutComPaymentTypeResolver;
        this.validators = validators;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class<?> clazz) {
        return PaymentDetailsWsDTO.class.isAssignableFrom(clazz);
    }

    /**
     * Validates the PaymentDataForm payment type and calls a specific validator based on the given payment type
     *
     * @param target PaymentDetailsWsDTO to validate
     * @param errors the binding results
     */
    @Override
    public void validate(final Object target, final Errors errors) {

        if (target instanceof PaymentDetailsWsDTO) {
            final PaymentDetailsWsDTO paymentDetailsWsDTO = (PaymentDetailsWsDTO) target;

            final List<String> validPaymentTypes = Arrays.stream(CheckoutComPaymentType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());

            if (StringUtils.isNotBlank(paymentDetailsWsDTO.getType()) && validPaymentTypes.contains(paymentDetailsWsDTO.getType())) {
                final String paymentType = paymentDetailsWsDTO.getType();
                final CheckoutComPaymentType checkoutComPaymentType = checkoutComPaymentTypeResolver.resolvePaymentMethod(paymentType);
                Optional.ofNullable(validators.get(checkoutComPaymentType))
                        .ifPresent(validator -> validator.validate(paymentDetailsWsDTO, errors));
            } else {
                errors.rejectValue(TYPE, "checkoutcom.paymentdata.form.type.invalid.error");
            }
        }
    }
}
