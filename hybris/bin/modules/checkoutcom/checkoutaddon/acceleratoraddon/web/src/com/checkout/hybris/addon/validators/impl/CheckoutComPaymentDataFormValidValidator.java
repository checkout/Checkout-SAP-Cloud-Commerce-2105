package com.checkout.hybris.addon.validators.impl;

import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import org.apache.commons.collections.MapUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Validates the PaymentDataForm and calls a specific validator based on the payment method
 */
public class CheckoutComPaymentDataFormValidValidator implements Validator {

    protected static final String TYPE = "type";

    protected final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    protected final Map<CheckoutComPaymentType, Validator> validators;

    public CheckoutComPaymentDataFormValidValidator(final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver,
                                                    final Map<CheckoutComPaymentType, Validator> validators) {
        this.checkoutComPaymentTypeResolver = checkoutComPaymentTypeResolver;
        this.validators = validators;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class<?> clazz) {
        return PaymentDataForm.class.isAssignableFrom(clazz);
    }

    /**
     * Validates the PaymentDataForm payment type and calls a specific validator based on the given payment type
     *
     * @param form   PaymentDataForm to validate
     * @param errors the binding results
     */
    @Override
    public void validate(final Object form, final Errors errors) {

        if (form instanceof PaymentDataForm) {
            final PaymentDataForm paymentDataForm = (PaymentDataForm) form;

            final List<String> validPaymentTypes = new ArrayList<>();
            Arrays.asList(CheckoutComPaymentType.values()).forEach(type -> validPaymentTypes.add(type.name()));

            if (MapUtils.isNotEmpty(paymentDataForm.getFormAttributes()) && paymentDataForm.getFormAttributes().containsKey(TYPE) && validPaymentTypes.contains(paymentDataForm.getFormAttributes().get(TYPE))) {
                final String paymentType = (String) paymentDataForm.getFormAttributes().get(TYPE);
                final CheckoutComPaymentType checkoutComPaymentType = checkoutComPaymentTypeResolver.resolvePaymentMethod(paymentType);
                final Validator validator = validators.get(checkoutComPaymentType);
                validator.validate(paymentDataForm, errors);
            } else {
                errors.rejectValue("formAttributes['" + TYPE + "']", "checkoutcom.paymentdata.form.type.invalid.error");
            }

        }
    }
}
