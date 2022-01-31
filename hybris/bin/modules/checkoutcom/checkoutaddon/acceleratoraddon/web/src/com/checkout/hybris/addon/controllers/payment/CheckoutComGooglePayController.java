package com.checkout.hybris.addon.controllers.payment;

import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.enums.WalletPaymentType;
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
@RequestMapping(value = "/checkout/payment/checkout-com/googlepay")
public class CheckoutComGooglePayController {

    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;

    /**
     * Places the google pay order
     *
     * @param authorisationRequest the google pay authorization request
     * @return the particular page for the scenario
     */
    @PostMapping(value = "/placeGooglePayOrder", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @RequireHardLogIn
    @ResponseBody
    public PlaceWalletOrderDataResponse authoriseOrder(@RequestBody final GooglePayAuthorisationRequest authorisationRequest) {

        checkoutComWalletAddressFacade.handleAndSaveAddresses(authorisationRequest.getBillingAddress());

        return checkoutComWalletOrderFacade.placeWalletOrder(authorisationRequest.getToken(), WalletPaymentType.GOOGLEPAY);
    }
}
