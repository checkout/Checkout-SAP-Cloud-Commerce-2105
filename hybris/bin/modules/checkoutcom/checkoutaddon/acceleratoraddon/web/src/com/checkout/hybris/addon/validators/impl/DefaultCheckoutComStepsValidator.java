package com.checkout.hybris.addon.validators.impl;

import com.checkout.hybris.addon.validators.CheckoutComStepsValidator;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Default implementation of the {@link CheckoutComStepsValidator}
 */
public class DefaultCheckoutComStepsValidator implements CheckoutComStepsValidator {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComStepsValidator.class);

    protected final CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    protected final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;

    public DefaultCheckoutComStepsValidator(final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                            final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade) {
        this.checkoutFlowFacade = checkoutFlowFacade;
        this.checkoutComPaymentInfoFacade = checkoutComPaymentInfoFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNoSessionCart(final Model model) {
        if (!checkoutFlowFacade.hasCheckoutCart()) {
            LOG.error("Session cart not found or empty.");
            GlobalMessages.addErrorMessage(model, "checkoutcom.error.session.cart.invalid");
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateCheckoutPlaceOrderStep(final RedirectAttributes redirectAttributes, final String securityCode) {
        final CartData cartData = checkoutFlowFacade.getCheckoutCart();

        return isDeliveryInfoInvalid(redirectAttributes) || isPaymentInfoInvalid(redirectAttributes, securityCode, cartData) || isCartPricingInvalid(redirectAttributes, cartData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTermsAndConditionsAccepted(final Model model, final boolean isTermsCheck) {
        if (!isTermsCheck) {
            LOG.error("Terms and conditions are not checked.");
            GlobalMessages.addErrorMessage(model, "checkoutcom.error.terms.not.accepted");
            return false;
        }
        return true;
    }

    protected boolean isDeliveryInfoInvalid(final RedirectAttributes redirectAttributes) {
        return isDeliveryAddressInvalid(redirectAttributes) || isDeliveryModeInvalid(redirectAttributes);
    }

    protected boolean isCartPricingInvalid(final RedirectAttributes redirectAttributes, final CartData cartData) {
        return hasCartInvalidTaxValues(redirectAttributes, cartData) || isCartNotCalculated(redirectAttributes, cartData);
    }

    protected boolean isCartNotCalculated(final RedirectAttributes redirectAttributes, final CartData cartData) {
        if (!cartData.isCalculated()) {
            LOG.error("Cart {} has a calculated flag of FALSE.", cartData.getCode());
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.error.cart.notcalculated");
            return true;
        }
        return false;
    }

    protected boolean hasCartInvalidTaxValues(final RedirectAttributes redirectAttributes, final CartData cartData) {
        if (!checkoutFlowFacade.containsTaxValues()) {
            LOG.error(
                    "Cart {} does not have any tax values, which means the tax calculation was not properly done.",
                    cartData.getCode());
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.error.tax.missing");
            return true;
        }
        return false;
    }

    protected boolean isPaymentInfoInvalid(final RedirectAttributes redirectAttributes, final String securityCode, final CartData cartData) {
        if (checkoutFlowFacade.hasNoPaymentInfo() || checkoutComPaymentInfoFacade.isTokenMissingOnCardPaymentInfo(cartData)) {
            LOG.error("Session cart has not payment info or the payment info does not have card token.");
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.paymentMethod.notSelected");
            return true;
        } else {
            // Only require the Security Code to be entered on the summary page if the SubscriptionPciOption is set to Default.
            if (CheckoutPciOptionEnum.DEFAULT.equals(checkoutFlowFacade.getSubscriptionPciOption())
                    && StringUtils.isBlank(securityCode)) {
                LOG.error("Security code is not populated.");
                GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.paymentMethod.noSecurityCode");
                return true;
            }
        }
        return false;
    }

    protected boolean isDeliveryModeInvalid(final RedirectAttributes redirectAttributes) {
        if (checkoutFlowFacade.hasNoDeliveryMode()) {
            LOG.error("Session cart has not delivery mode.");
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.deliveryMethod.notSelected");
            return true;
        }
        return false;
    }

    protected boolean isDeliveryAddressInvalid(final RedirectAttributes redirectAttributes) {
        if (checkoutFlowFacade.hasNoDeliveryAddress()) {
            LOG.error("Session cart has not delivery address.");
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.deliveryAddress.notSelected");
            return true;
        }
        return false;
    }
}
