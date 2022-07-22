package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.GooglePayIntermediatePaymentData;
import com.checkout.hybris.facades.beans.GooglePayPaymentDataRequestUpdate;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
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
import java.util.Optional;

@Controller
@RequestMapping(value = "/checkout/payment/checkout-com/googlepay")
public class CheckoutComGooglePayController {

    @Resource(name = "userFacade")
    protected UserFacade userFacade;
    @Resource(name = "cartFacade")
    protected CartFacade cartFacade;
    @Resource(name = "customerFacade")
    protected CheckoutComCustomerFacade checkoutComCustomerFacade;
    @Resource(name = "guidCookieStrategy")
    protected GUIDCookieStrategy guidCookieStrategy;
    @Resource
    protected CheckoutComGooglePayFacade checkoutComGooglePayFacade;
    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;
    @Resource(name = "checkoutComCheckoutExpressPlaceOrderCartValidator")
    protected Validator checkoutComPlaceOrderCartValidator;

    /**
     * Places the google pay order
     *
     * @param authorisationRequest the google pay authorization request
     * @return the particular page for the scenario
     */
    @PostMapping(value = "/placeGooglePayOrder", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @RequireHardLogIn
    @ResponseBody
    public PlaceWalletOrderDataResponse authoriseOrder(@RequestBody final GooglePayAuthorisationRequest authorisationRequest) throws InvalidCartException {
        if (checkoutComCustomerFacade.isGooglePayExpressGuestCustomer()) {
            checkoutComCustomerFacade.updateExpressCheckoutUserEmail(authorisationRequest.getEmail(), authorisationRequest.getShippingAddress().getName());
        }

        Optional.ofNullable(authorisationRequest.getBillingAddress()).ifPresent(checkoutComWalletAddressFacade::handleAndSaveBillingAddress);
        Optional.ofNullable(authorisationRequest.getShippingAddress()).ifPresent(checkoutComWalletAddressFacade::handleAndSaveShippingAddress);

        checkoutComWalletOrderFacade.validateCartForPlaceOrder(checkoutComPlaceOrderCartValidator);
        return checkoutComWalletOrderFacade.placeWalletOrder(authorisationRequest.getToken(), WalletPaymentType.GOOGLEPAY);
    }


    @PostMapping(value = "/deliveryInfo", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @ApiOperation(nickname = "getGooglePayDeliveryInfo", value = "Creates a delivery address and delivery method for the cart.")
    public GooglePayPaymentDataRequestUpdate getGooglePayDeliveryInfo(final HttpServletRequest request, final HttpServletResponse response, @ApiParam(required = true) @RequestBody final GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) throws DuplicateUidException {
        if (userFacade.isAnonymousUser()) {
            checkoutComCustomerFacade.createGooglePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession();
            guidCookieStrategy.setCookie(request, response);
        }
        return checkoutComGooglePayFacade.getGooglePayDeliveryInfo(googlePayIntermediatePaymentData);
    }

    @GetMapping(value = "/clearCartAndAddToCart", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @ApiOperation(nickname = "clearCurrentCartAndAddToCart", value = "Removes current cart and add the product to a new cart")
    public void clearCurrentCartAndAddToCart(@RequestParam(defaultValue = "1") final long quantity, @RequestParam final String productCode) throws CommerceCartModificationException {
        cartFacade.removeSessionCart();
        cartFacade.addToCart(productCode, quantity);
    }

}
