package com.checkout.hybris.occ.validators.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Checkout commerce web services cart validator. Checks if cart is calculated and if needed values are filled.
 */
public class CheckoutComPlaceOrderCartValidator implements Validator {

    private static final Logger LOG = LogManager.getLogger(CheckoutComPlaceOrderCartValidator.class);

    protected final CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    protected final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;

    public CheckoutComPlaceOrderCartValidator(final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                              final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade) {
        this.checkoutFlowFacade = checkoutFlowFacade;
        this.checkoutComPaymentInfoFacade = checkoutComPaymentInfoFacade;
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        return CartData.class.equals(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final CartData cartData = (CartData) target;

        validateDeliveryInfoInvalid(errors);
        validateIsPaymentInfoInvalid(errors, cartData);
        validateCartPricingInvalid(errors, cartData);
    }

    /**
     * Validates the delivery the delivery
     *
     * @param errors The errors
     */
    protected void validateDeliveryInfoInvalid(final Errors errors) {
        validateDeliveryAddressInvalid(errors);
        validateIsDeliveryModeInvalid(errors);
    }

    /**
     * Validates the cart pricing
     * @param errors The errors
     * @param cartData The cart to validate
     */
    protected void validateCartPricingInvalid(final Errors errors, final CartData cartData) {
        validateHasCartInvalidTaxValues(errors, cartData);
        validateCartNotCalculated(errors, cartData);
    }

    /**
     * Validates if cart is calculated
     * @param errors The errors
     * @param cartData The cart to validate
     */
    protected void validateCartNotCalculated(final Errors errors, final CartData cartData) {
        if (!cartData.isCalculated()) {
            LOG.error("Cart {} has a calculated flag of FALSE.", cartData.getCode());
            errors.reject("checkoutcom.occ.error.cart.notcalculated");
        }
    }

    /**
     * Validates the cart tax values
     * @param errors The errors
     * @param cartData The cart to validate
     */
    protected void validateHasCartInvalidTaxValues(final Errors errors, final CartData cartData) {
        if (!checkoutFlowFacade.containsTaxValues()) {
            LOG.error(
                    "Cart {} does not have any tax values, which means the tax calculation was not properly done.",
                    cartData.getCode());
            errors.reject("checkoutcom.occ.error.tax.missing");
        }
    }

    /**
     * Validates the payment info
     * @param errors The errors
     * @param cartData The cart to validate
     */
    protected void validateIsPaymentInfoInvalid(final Errors errors, final CartData cartData) {
        if (checkoutFlowFacade.hasNoPaymentInfo() || checkoutComPaymentInfoFacade.isTokenMissingOnCardPaymentInfo(cartData)) {
            LOG.error("Session cart has not payment info or the payment info does not have card token.");
            errors.reject("checkoutcom.occ.paymentMethod.notSelected");
        }
    }

    /**
     * Validates the delivery mode
     * @param errors The errors
     */
    protected void validateIsDeliveryModeInvalid(final Errors errors) {
        if (checkoutFlowFacade.hasNoDeliveryMode()) {
            LOG.error("Session cart has not delivery mode.");
            errors.reject("checkoutcom.occ.deliveryMethod.notSelected");
        }
    }

    /**
     * Validates the delivery address
     * @param errors The errors
     */
    protected void validateDeliveryAddressInvalid(final Errors errors) {
        if (checkoutFlowFacade.hasNoDeliveryAddress()) {
            LOG.error("Session cart has not delivery address.");
            errors.reject("checkoutcom.occ.deliveryAddress.notSelected");
        }
    }
}
