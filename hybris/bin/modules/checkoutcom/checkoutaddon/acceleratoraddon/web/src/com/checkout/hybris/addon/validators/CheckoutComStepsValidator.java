package com.checkout.hybris.addon.validators;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Validator to validate the checkout.com steps
 */
public interface CheckoutComStepsValidator {

    /**
     * Checks is the session cart is still valid
     *
     * @param model the view model
     * @return true if there is an error, otherwise false
     */
    boolean hasNoSessionCart(Model model);

    /**
     * Validate the cart datas and form parameters before authorize and place the order.
     *
     * @param redirectAttributes the redirect view model
     * @param securityCode       the card security code
     * @return true if there is an error, otherwise false
     */
    boolean validateCheckoutPlaceOrderStep(RedirectAttributes redirectAttributes, String securityCode);

    /**
     * Checks if terms and conditions are accepted
     *
     * @param model        the view model
     * @param isTermsCheck the terms and conditions acceptance
     * @return true if is valid, otherwise false
     */
    boolean isTermsAndConditionsAccepted(Model model, boolean isTermsCheck);
}
