package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.ApplePayPaymentRequestWsDTO;
import com.checkout.dto.order.ApplePayValidateMerchantRequestWsDTO;
import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.ApplePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.ApplePayPaymentRequestData;
import com.checkout.hybris.facades.beans.ApplePayValidateMerchantRequestData;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;

@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts/{cartId}/applepay")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Orders")
public class CheckoutComApplePayOrderController {

    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;
    @Resource
    protected CheckoutComApplePayFacade checkoutComApplePayFacade;
    @Resource
    protected DataMapper dataMapper;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @GetMapping(value = "/paymentRequest", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "getPaymentRequest", value = "Gets apple pay payment request")
    public ResponseEntity<ApplePayPaymentRequestWsDTO> getPaymentRequest() {
        final ApplePayPaymentRequestData data = checkoutComApplePayFacade.getApplePayPaymentRequest();
        return ResponseEntity.ok().body(dataMapper.map(data, ApplePayPaymentRequestWsDTO.class, FieldSetLevelHelper.FULL_LEVEL));
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/requestSession", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "requestSession", value = "Validates the session for apple pay")
    public Object requestPaymentSession(@RequestBody final ApplePayValidateMerchantRequestWsDTO validateMerchantRequestData) {
        final ApplePayValidateMerchantRequestData data = new ApplePayValidateMerchantRequestData();
        data.setValidationURL(validateMerchantRequestData.getValidationURL());

        return checkoutComApplePayFacade.requestApplePayPaymentSession(data);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/placeOrder")
    @ApiOperation(nickname = "placeOrder", value = "Place a order with apple pay.", notes = "Authorizes the cart and places the order. The response contains a place wallet order data.")
    public PlaceWalletOrderWsDTO placeOrder(@RequestBody final ApplePayAuthorisationRequest authorisationRequest,
                                            @RequestParam(defaultValue = DEFAULT_LEVEL) final String fields) {
        checkoutComWalletAddressFacade.handleAndSaveAddresses(authorisationRequest.getBillingContact());

        final PlaceWalletOrderDataResponse placeWalletOrderData = checkoutComWalletOrderFacade
                .placeWalletOrder(authorisationRequest.getToken().getPaymentData(), WalletPaymentType.APPLEPAY);

        return dataMapper.map(placeWalletOrderData, PlaceWalletOrderWsDTO.class, fields);
    }
}
