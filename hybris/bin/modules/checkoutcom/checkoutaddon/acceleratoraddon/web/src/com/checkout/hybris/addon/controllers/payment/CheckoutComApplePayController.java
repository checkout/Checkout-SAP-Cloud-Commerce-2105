package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.order.InvalidCartException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/checkout/payment/checkout-com/applepay")
public class CheckoutComApplePayController {

    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;
    @Resource
    protected CheckoutComApplePayFacade checkoutComApplePayFacade;
    @Resource(name = "userFacade")
    protected UserFacade userFacade;
    @Resource(name = "customerFacade")
    protected CheckoutComCustomerFacade checkoutComCustomerFacade;
    @Resource(name = "checkoutComCheckoutExpressPlaceOrderCartValidator")
    protected Validator checkoutComCheckoutExpressPlaceOrderCartValidator;
    @Resource(name = "checkoutFlowFacade")
    protected CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade;
    @Resource(name = "guidCookieStrategy")
    protected GUIDCookieStrategy guidCookieStrategy;


    /**
     * Validates the session for apple pay
     *
     * @param validateMerchantRequestData the validate session request
     * @return the response
     */
    @PostMapping(value = "/request-session", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public Object requestPaymentSession(@RequestBody final ApplePayValidateMerchantRequestData validateMerchantRequestData) {
        return checkoutComApplePayFacade.requestApplePayPaymentSession(validateMerchantRequestData);
    }

    /**
     * Places the apple pay order
     *
     * @param authorisationRequest the apple pay authorization request
     * @return the particular page for the scenario
     */
    @PostMapping(value = "/placeApplePayOrder")
    @RequireHardLogIn
    @ResponseBody
    public PlaceWalletOrderDataResponse placeApplePayOrder(@RequestBody final ApplePayAuthorisationRequest authorisationRequest) throws InvalidCartException {
        if (checkoutComCustomerFacade.isApplePayExpressGuestCustomer()) {
            checkoutComCustomerFacade.updateExpressCheckoutUserEmail(authorisationRequest.getShippingContact().getEmailAddress(), authorisationRequest.getShippingContact().getGivenName());
        }
        if (authorisationRequest.getBillingContact() != null) {
            checkoutComWalletAddressFacade.handleAndSaveBillingAddress(authorisationRequest.getBillingContact());
        }
        if (authorisationRequest.getShippingContact() != null) {
            checkoutComWalletAddressFacade.handleAndSaveShippingAddress(authorisationRequest.getShippingContact());
        }
        checkoutComWalletOrderFacade.validateCartForPlaceOrder(checkoutComCheckoutExpressPlaceOrderCartValidator);
        return checkoutComWalletOrderFacade.placeWalletOrder(authorisationRequest.getToken().getPaymentData(), WalletPaymentType.APPLEPAY);
    }

    /**
     * Sets the delivery method selected
     *
     * @return the cart info updated after the delivery method has been set
     */
    @PostMapping(value = "/deliveryMethod")
    @RequireHardLogIn
    @ResponseBody
    public ApplePayShippingMethodUpdate setDeliveryMode(@ApiParam(required = true) @RequestBody final ApplePayShippingMethod applePayShippingMethod) {
        checkoutComCheckoutFlowFacade.setDeliveryMode(applePayShippingMethod.getIdentifier());
        return checkoutComApplePayFacade.getApplePayShippingMethodUpdate();
    }

    @PostMapping(value = "/deliveryAddress", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @ApiOperation(nickname = "setDeliveryAddress", value = "Creates a delivery address for the cart.")
    public ApplePayShippingContactUpdate setDeliveryAddress(final HttpServletRequest request, final HttpServletResponse response, @ApiParam(required = true) @RequestBody final ApplePayPaymentContact applePayPaymentContact) throws DuplicateUidException {
        if (userFacade.isAnonymousUser()) {
            checkoutComCustomerFacade.createApplePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession();
            guidCookieStrategy.setCookie(request, response);
        }
        checkoutComWalletAddressFacade.handleAndSaveShippingAddress(applePayPaymentContact);
        return checkoutComApplePayFacade.getApplePayShippingContactUpdate();
    }
}
