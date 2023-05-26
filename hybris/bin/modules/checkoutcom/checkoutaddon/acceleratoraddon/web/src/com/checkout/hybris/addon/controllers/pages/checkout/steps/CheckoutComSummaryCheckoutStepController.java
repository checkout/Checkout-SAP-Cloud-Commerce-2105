package com.checkout.hybris.addon.controllers.pages.checkout.steps;

import com.checkout.hybris.addon.constants.CheckoutaddonConstants;
import com.checkout.hybris.addon.controllers.CheckoutaddonControllerConstants;
import com.checkout.hybris.addon.validators.CheckoutComStepsValidator;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PlaceOrderForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.Arrays;

import static com.checkout.hybris.addon.constants.CheckoutaddonWebConstants.*;
import static de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages.ERROR_MESSAGES_HOLDER;
import static de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages.addFlashMessage;

/**
 * Controller for the summary checkout step
 */
@Controller
@RequestMapping(value = "/checkout/multi/checkout-com/summary")
public class CheckoutComSummaryCheckoutStepController extends AbstractCheckoutStepController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComSummaryCheckoutStepController.class);
    protected static final String SUMMARY = "summary";
    protected static final String CHECKOUT_SUMMARY_PLACE_ORDER_URL = "/checkout/multi/checkout-com/summary/placeOrder";
    protected static final String CHECKOUTCOM_SUMMARY_CMS_PAGE_LABEL = "checkoutComSummaryPage";
    protected static final String REQUEST_SECURITY_CODE_MODEL_ATTRIBUTE_KEY = "requestSecurityCode";
    protected static final String PLACE_ORDER_URL_MODEL_ATTRIBUTE_KEY = "placeOrderUrl";
    protected static final String PAYMENT_INFO_MODEL_ATTRIBUTE_KEY = "paymentInfo";
    protected static final String DELIVERY_MODE_MODEL_ATTRIBUTE_KEY = "deliveryMode";
    protected static final String ALL_ITEMS_MODEL_ATTRIBUTE_KEY = "allItems";

    @Resource
    private CheckoutComStepsValidator checkoutComStepsValidator;
    @Resource
    private CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    @Resource
    private ConfigurationService configurationService;

    /**
     * Shows the summary checkout step
     *
     * @param model              the model
     * @param redirectAttributes the redirect attributes
     * @return the checkout summary step
     * @throws CMSItemNotFoundException
     */
    @GetMapping(value = "/view")
    @RequireHardLogIn
    @Override
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = SUMMARY)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        if (cartData.getEntries() != null && !cartData.getEntries().isEmpty()) {
            for (final OrderEntryData entry : cartData.getEntries()) {
                final String productCode = entry.getProduct().getCode();
                final ProductData product = getProductFacade().getProductForCodeAndOptions(productCode, Arrays.asList(
                        ProductOption.BASIC, ProductOption.PRICE, ProductOption.VARIANT_MATRIX_BASE, ProductOption.PRICE_RANGE));
                entry.setProduct(product);
            }
        }

        model.addAttribute(CART_DATA_MODEL_ATTRIBUTE_KEY, cartData);
        model.addAttribute(ALL_ITEMS_MODEL_ATTRIBUTE_KEY, cartData.getEntries());
        model.addAttribute(DELIVERY_ADDRESS_MODEL_ATTRIBUTE_KEY, cartData.getDeliveryAddress());
        model.addAttribute(DELIVERY_MODE_MODEL_ATTRIBUTE_KEY, cartData.getDeliveryMode());
        model.addAttribute(PAYMENT_INFO_MODEL_ATTRIBUTE_KEY, cartData.getPaymentInfo());
        model.addAttribute(PLACE_ORDER_URL_MODEL_ATTRIBUTE_KEY, CHECKOUT_SUMMARY_PLACE_ORDER_URL);

        // Only request the security code if the SubscriptionPciOption is set to Default.
        final boolean requestSecurityCode = CheckoutPciOptionEnum.DEFAULT
                .equals(getCheckoutFlowFacade().getSubscriptionPciOption());
        model.addAttribute(REQUEST_SECURITY_CODE_MODEL_ATTRIBUTE_KEY, requestSecurityCode);
        model.addAttribute(new PlaceOrderForm());

        final ContentPageModel checkoutComSummaryPage = getContentPageForLabelOrId(CHECKOUTCOM_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, checkoutComSummaryPage);
        setUpMetaDataForContentPage(model, checkoutComSummaryPage);

        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.summary.breadcrumb"));
        model.addAttribute(META_ROBOTS_MODEL_ATTRIBUTE_KEY, "noindex,nofollow");
        setCheckoutStepLinksForModel(model, getCheckoutStep());
        return configurationService.getConfiguration().getString(CheckoutaddonConstants.CHECKOUT_ADDON_PREFIX) +
                CheckoutaddonControllerConstants.Views.Pages.MultiStepCheckout.CheckoutSummaryPage;
    }

    /**
     * Validates the cart and the placeOrderForm, then places the order
     *
     * @param placeOrderForm     the place order form
     * @param model              the model
     * @param redirectAttributes the redirect attributes
     * @return the order confirmation if everything is fine
     * @throws CMSItemNotFoundException
     */
    @RequestMapping(value = "/placeOrder")
    @PreValidateQuoteCheckoutStep
    @RequireHardLogIn
    public String placeOrder(@ModelAttribute("placeOrderForm") final PlaceOrderForm placeOrderForm, final Model model,
                             final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {

        if (checkoutComStepsValidator.hasNoSessionCart(redirectAttributes) || validateCart(redirectAttributes)) {
            return REDIRECT_PREFIX + "/cart";
        }

        if (!checkoutComStepsValidator.isTermsAndConditionsAccepted(model, placeOrderForm.isTermsCheck())) {
            return enterStep(model, redirectAttributes);
        }

        if (checkoutComStepsValidator.validateCheckoutPlaceOrderStep(redirectAttributes, placeOrderForm.getSecurityCode())) {
            return redirectToChoosePaymentMethodStep();
        }

        return authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);
    }

    /**
     * When the customer is already authenticated and has a default payment method in his account, checking the
     * express checkout checkbox can directly land to this step
     *
     * @param model              the model
     * @param redirectAttributes the redirect attributes
     * @return the checkout summary step
     * @throws CMSItemNotFoundException
     */
    @GetMapping(value = "/express")
    @RequireHardLogIn
    public String performExpressCheckout(final Model model, final RedirectAttributes redirectAttributes)
            throws CMSItemNotFoundException {

        if (getSessionService().getAttribute(WebConstants.CART_RESTORATION) != null
                && CollectionUtils.isNotEmpty(((CartRestorationData) getSessionService().getAttribute(WebConstants.CART_RESTORATION))
                .getModifications())) {
            return REDIRECT_URL_CART;
        }

        String returnUrl = REDIRECT_URL_CART;

        if (getCheckoutFlowFacade().hasValidCart()) {
            switch (getCheckoutFacade().performExpressCheckout()) {
                case SUCCESS:
                    returnUrl = enterStep(model, redirectAttributes);
                    break;
                case ERROR_DELIVERY_ADDRESS:
                    addFlashMessage(redirectAttributes, ERROR_MESSAGES_HOLDER, "checkout.express.error.deliveryAddress");
                    returnUrl = REDIRECT_URL_ADD_DELIVERY_ADDRESS;
                    break;
                case ERROR_DELIVERY_MODE:
                case ERROR_CHEAPEST_DELIVERY_MODE:
                    addFlashMessage(redirectAttributes, ERROR_MESSAGES_HOLDER, "checkout.express.error.deliveryMode");
                    returnUrl = REDIRECT_URL_CHOOSE_DELIVERY_METHOD;
                    break;
                case ERROR_PAYMENT_INFO:
                    addFlashMessage(redirectAttributes, ERROR_MESSAGES_HOLDER, "checkout.express.error.paymentInfo");
                    returnUrl = redirectToChoosePaymentMethodStep();
                    break;
                default:
                    addFlashMessage(redirectAttributes, ERROR_MESSAGES_HOLDER, "checkout.express.error.notAvailable");
            }
        }
        return returnUrl;
    }

    /**
     * Removes the payment info from the cart and redirect to choose payment method step
     *
     * @return the redirect to choose payment method step
     */
    protected String redirectToChoosePaymentMethodStep() {
        checkoutFlowFacade.removePaymentInfoFromSessionCart();
        return REDIRECT_TO_CHOOSE_PAYMENT_METHOD;
    }

    @GetMapping(value = "/back")
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @GetMapping(value = "/next")
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(SUMMARY);
    }

    protected String authorisePlaceOrderAndRedirectToResultPage(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        final AuthorizeResponseData authorizeResponseData = checkoutFlowFacade.authorizePayment();

        if (!authorizeResponseData.getIsSuccess() || Boolean.TRUE.equals(authorizeResponseData.getIsRedirect())) {
            if (Boolean.TRUE.equals(authorizeResponseData.getIsRedirect())) {
                LOG.debug("Redirecting to checkout.com url [{}] for 3d secure.", authorizeResponseData.getRedirectUrl());
                return REDIRECT_PREFIX + authorizeResponseData.getRedirectUrl();
            } else {
                LOG.error("Error with the authorization process. Redirecting to payment method step.");
                GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.error.authorization.failed");
                return redirectToChoosePaymentMethodStep();
            }
        }

        final OrderData orderData;
        try {
            orderData = getCheckoutFacade().placeOrder();
        }
        catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            checkoutFlowFacade.removePaymentInfoFromSessionCart();
            GlobalMessages.addErrorMessage(model, "checkout.placeOrder.failed");
            return enterStep(model, redirectAttributes);
        }

        return redirectToOrderConfirmationPage(orderData);
    }

    @Override
    protected String redirectToOrderConfirmationPage(final OrderData orderData) {
        return super.redirectToOrderConfirmationPage(orderData);
    }
}
