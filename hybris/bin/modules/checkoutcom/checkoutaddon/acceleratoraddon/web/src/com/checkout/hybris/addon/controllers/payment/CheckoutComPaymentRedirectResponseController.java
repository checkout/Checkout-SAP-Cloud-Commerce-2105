package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.checkout.hybris.addon.constants.CheckoutaddonWebConstants.REDIRECT_TO_CHOOSE_PAYMENT_METHOD;

/**
 * Controller to handle payments redirect responses
 */
@Controller
@RequestMapping(value = "/checkout/payment/checkout-com/redirect-response")
public class CheckoutComPaymentRedirectResponseController extends AbstractCheckoutController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComPaymentRedirectResponseController.class);

    protected static final String PARAM_CKO_SESSION_ID = "cko-session-id";

    @Resource
    protected CheckoutComPaymentFacade checkoutComPaymentFacade;
    @Resource
    protected CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    @Resource
    protected CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;

    /**
     * Handles the checkout.com redirect after the 3d secure in case of success. Places the order if everything is fine and redirects
     * to order confirmation page.
     *
     * @param request            the http request
     * @param redirectAttributes the redirect attributes
     * @return the order confirmation page
     */
    @GetMapping(value = {"/success"})
    @RequireHardLogIn
    public String handleSuccessRedirect(final HttpServletRequest request, final RedirectAttributes redirectAttributes) {

        if (!request.getParameterMap().containsKey(PARAM_CKO_SESSION_ID) || StringUtils.isBlank(request.getParameter(PARAM_CKO_SESSION_ID))) {
            LOG.error("Could not find valid cko-session-id");
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.placeOrder.failed");
            return REDIRECT_PREFIX + "/";
        }

        boolean cartMatch = false;

        final String checkoutSessionId = getCkoSessionId(request);

        Optional<GetPaymentResponse> paymentDetails;
        try {
            paymentDetails = checkoutComPaymentFacade.getPaymentDetailsByCkoSessionId(checkoutSessionId);
            if (paymentDetails.isPresent()) {
                cartMatch = checkoutComPaymentFacade.doesSessionCartMatchAuthorizedCart(paymentDetails.get());
            }
        } catch (final CheckoutComPaymentIntegrationException e) {
            LOG.error("Exception when getting the payment from session id from checkout.com", e);
            // we need to void the authorization as well before the redirect
            return handleFailureRedirect(redirectAttributes);
        }

        if (!cartMatch) {
            LOG.error("Session cart matching not found.");
            return REDIRECT_PREFIX + "/";
        }

        paymentDetails.ifPresent(responseDetails -> checkoutComPaymentInfoFacade.processPaymentDetails(paymentDetails.get()));

        final OrderData orderData;
        try {
            orderData = getCheckoutFacade().placeOrder();
        } catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.placeOrder.failed");
            return redirectToChoosePaymentMethodStep();
        }

        return redirectToOrderConfirmationPage(orderData);
    }

    /**
     * Handles the checkout.com redirect after the 3d secure in case of failure. Redirects to payment method step with error
     *
     * @param redirectAttributes the redirect attributes
     * @return the payment method step
     */
    @GetMapping(value = {"/failure"})
    @RequireHardLogIn
    public String handleFailureRedirect(final RedirectAttributes redirectAttributes) {
        GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.error.authorization.failed");
        return redirectToChoosePaymentMethodStep();
    }

    /**
     * Gets the cko session id from the request
     *
     * @param request the HttpServletRequest
     * @return the checkoutSessionId
     */
    protected String getCkoSessionId(final HttpServletRequest request) {
        final String checkoutSessionId = request.getParameter(PARAM_CKO_SESSION_ID);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Received success redirect from payment with cko-session-id [{}]", checkoutSessionId);
        }
        return checkoutSessionId;
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
}
