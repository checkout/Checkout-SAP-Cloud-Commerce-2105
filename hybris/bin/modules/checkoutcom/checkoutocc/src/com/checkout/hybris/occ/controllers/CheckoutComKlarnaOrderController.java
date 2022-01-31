package com.checkout.hybris.occ.controllers;

import com.checkout.hybris.facades.beans.KlarnaClientTokenData;

import com.checkout.hybris.facades.payment.klarna.CheckoutComKlarnaFacade;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts/{cartId}/klarna")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Orders")
public class CheckoutComKlarnaOrderController {

    @Resource
    protected CheckoutComKlarnaFacade checkoutComKlarnaFacade;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @GetMapping(value = "/clientToken", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "getClientToken", value = "Gets the klarna client token")
    public ResponseEntity<KlarnaClientTokenData> getClientToken() throws ExecutionException {
        return ResponseEntity.ok().body(checkoutComKlarnaFacade.getKlarnaClientToken());
    }
}
