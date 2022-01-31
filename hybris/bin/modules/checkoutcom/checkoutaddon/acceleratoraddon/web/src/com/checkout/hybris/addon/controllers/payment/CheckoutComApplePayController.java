package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.ApplePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.ApplePayValidateMerchantRequestData;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/checkout/payment/checkout-com/applepay")
public class CheckoutComApplePayController {

    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;
    @Resource
    protected CheckoutComApplePayFacade checkoutComApplePayFacade;

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
    public PlaceWalletOrderDataResponse placeApplePayOrder(@RequestBody final ApplePayAuthorisationRequest authorisationRequest) {

        checkoutComWalletAddressFacade.handleAndSaveAddresses(authorisationRequest.getBillingContact());
        return checkoutComWalletOrderFacade.placeWalletOrder(authorisationRequest.getToken().getPaymentData(), WalletPaymentType.APPLEPAY);
    }
}
