package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.GooglePayMerchantConfigurationWsDTO;
import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.checkout.hybris.facades.enums.WalletPaymentType.GOOGLEPAY;
import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;

@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts/{cartId}/google")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Orders")
public class CheckoutComGooglePayOrderController {

    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;
    @Resource
    protected DataMapper dataMapper;
    @Resource
    protected CheckoutComGooglePayFacade checkoutComGooglePayFacade;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @GetMapping(value = "/merchant-configuration", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "getMerchantConfiguration", value = "Gets google pay payment request")
    public ResponseEntity<GooglePayMerchantConfigurationWsDTO> getMerchantConfiguration() {
        return ResponseEntity.ok().body(dataMapper.map(checkoutComGooglePayFacade.getGooglePayMerchantConfiguration(), GooglePayMerchantConfigurationWsDTO.class, FieldSetLevelHelper.FULL_LEVEL));
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/placeOrder", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(nickname = "placeOrder", value = "Place a order with google pay.", notes = "Authorizes the cart and places the order. The response contains a place wallet order data.")
    public PlaceWalletOrderWsDTO placeOrder(@RequestBody final GooglePayAuthorisationRequest authorisationRequest,
                                            @RequestParam(defaultValue = DEFAULT_LEVEL) final String fields) {
        checkoutComWalletAddressFacade.handleAndSaveAddresses(authorisationRequest.getBillingAddress());

        final PlaceWalletOrderDataResponse placeWalletOrderData = checkoutComWalletOrderFacade.placeWalletOrder(authorisationRequest.getToken(), GOOGLEPAY);
        return dataMapper.map(placeWalletOrderData, PlaceWalletOrderWsDTO.class, fields);
    }
}
