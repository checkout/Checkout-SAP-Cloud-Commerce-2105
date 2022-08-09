package com.checkout.hybris.facades.cart.validators.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Checkout cart validator. Checks if cart is calculated and if needed values are filled.
 */
public class CheckoutComCheckoutExpressPlaceOrderCartValidator extends CheckoutComPlaceOrderCartValidator implements Validator {

    public CheckoutComCheckoutExpressPlaceOrderCartValidator(final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                                             final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade) {
        super(checkoutFlowFacade, checkoutComPaymentInfoFacade);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final CartData cartData = (CartData) target;

        validateDeliveryInfoInvalid(errors);
        validateCartPricingInvalid(errors, cartData);
    }
}
