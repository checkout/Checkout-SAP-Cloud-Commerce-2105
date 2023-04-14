package com.checkout.hybris.occ.controllers;

import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(value = "/{baseSiteId}/merchantKey")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Merchant Configuration")
public class CheckoutComMerchantController {

    private static final String IS_ABC_FALSE = "false";
    @Resource
    private CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getMerchantKey() {

        final String publicKey = checkoutComMerchantConfigurationFacade.getCheckoutComMerchantPublicKey();
        return StringUtils.isBlank(publicKey) ? ResponseEntity.status(INTERNAL_SERVER_ERROR).body("Merchant key is empty or null")
                : ResponseEntity.ok().body(publicKey);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @GetMapping (value = "/isABC", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> isMerchantABC() {
        Boolean isABC = checkoutComMerchantConfigurationFacade.isCheckoutComMerchantABC();
        return isABC == null ? ResponseEntity.status(INTERNAL_SERVER_ERROR).body(IS_ABC_FALSE) :
                ResponseEntity.ok().body(isABC.toString());
    }
}
