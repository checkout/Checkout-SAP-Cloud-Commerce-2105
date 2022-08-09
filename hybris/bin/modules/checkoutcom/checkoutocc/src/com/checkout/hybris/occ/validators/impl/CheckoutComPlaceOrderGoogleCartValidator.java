package com.checkout.hybris.occ.validators.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.cart.validators.impl.CheckoutComPlaceOrderCartValidator;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.springframework.validation.Errors;

/**
 * Validates the delivery info and cart pricing for google pay method
 */
public class CheckoutComPlaceOrderGoogleCartValidator extends CheckoutComPlaceOrderCartValidator {

    public CheckoutComPlaceOrderGoogleCartValidator(final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                                    final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade) {
        super(checkoutFlowFacade, checkoutComPaymentInfoFacade);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object target, final Errors errors) {
        final CartData cartData = (CartData) target;

        callSuperMethods(errors, cartData);
    }

    /**
     * Call super methods to validate the delivery info and cart prices
     *
     * @param errors   the validation errors
     * @param cartData the cart to  validate
     */
    protected void callSuperMethods(final Errors errors, final CartData cartData) {
        validateDeliveryInfoInvalid(errors);
        validateCartPricingInvalid(errors, cartData);
    }
}
