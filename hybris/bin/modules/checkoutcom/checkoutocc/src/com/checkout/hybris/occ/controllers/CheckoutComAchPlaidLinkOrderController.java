package com.checkout.hybris.occ.controllers;

import com.checkout.dto.plaidlink.PlaidLinkCreationResponseDTO;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.checkout.hybris.facades.payment.CheckoutComACHConsentFacade;
import com.checkout.hybris.facades.payment.ach.CheckoutComAchFacade;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;
import com.checkout.hybris.facades.payment.plaidlink.CheckoutComPlaidLinkFacade;
import com.checkout.hybris.occ.exceptions.PlaceOrderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdUserIdAndCartIdParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts/{cartId}/ach")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Orders")
public class CheckoutComAchPlaidLinkOrderController {

    private static final Logger LOG = LogManager.getLogger(CheckoutComAchPlaidLinkOrderController.class);

    @Resource(name = "dataMapper")
    private DataMapper dataMapper;
    @Resource
    protected CheckoutComAchFacade checkoutComACHFacade;
    @Resource
    private AcceleratorCheckoutFacade acceleratorCheckoutFacade;
    @Resource
    protected CheckoutComPlaidLinkFacade checkoutComPlaidLinkFacade;
    @Resource
    protected CheckoutComACHConsentFacade checkoutComACHConsentFacade;
    @Resource
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/link/token/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "linkTokenCreate", value = "Gets the ACH link token")
    @ApiBaseSiteIdUserIdAndCartIdParam
    public ResponseEntity<String> linkTokenCreate() throws IOException {
        return ResponseEntity.ok().body(new ObjectMapper().writeValueAsString(checkoutComPlaidLinkFacade.linkTokenCreate()));
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/item/public_token/exchange", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "itemPublicTokenExchange", value = "Gets the ACH access token")
    @ApiBaseSiteIdUserIdAndCartIdParam
    public OrderWsDTO itemPublicTokenExchange(
            @RequestBody final PlaidLinkCreationResponseDTO plaidLinkCreationResponseDTO) throws IOException,
            PlaceOrderException, PaymentAuthorizationException, CustomerConsentException {
        final PlaidLinkCreationResponse plaidLinkCreationResponse = dataMapper.map(plaidLinkCreationResponseDTO,
                                                                                   PlaidLinkCreationResponse.class);
        final AchBankInfoDetailsData bankAccountDetailsData = checkoutComPlaidLinkFacade.getBankAccountDetailsData(
            plaidLinkCreationResponse);
        checkoutComACHFacade.setPaymentInfoAchToCart(bankAccountDetailsData);
        checkoutComACHConsentFacade.createCheckoutComACHConsent(bankAccountDetailsData,
                                                                plaidLinkCreationResponseDTO.getCustomerConsents());

        return authorisePlaceOrderAndRedirectToResultPage();
    }

    protected OrderWsDTO authorisePlaceOrderAndRedirectToResultPage() throws PlaceOrderException, PaymentAuthorizationException {
        final AuthorizeResponseData authorizeResponseData = checkoutComCheckoutFlowFacade.authorizePayment();
        checkSuccessfulAuth(authorizeResponseData);
        final OrderData orderData = placeOrder();
        return convertOrderDataToDTO(orderData);
    }

    protected OrderData placeOrder() throws PlaceOrderException {
        final OrderData orderData;
        try {
            orderData = acceleratorCheckoutFacade.placeOrder();
        } catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            checkoutComCheckoutFlowFacade.removePaymentInfoFromSessionCart();
            throw new PlaceOrderException();
        }
        return orderData;
    }

    protected OrderWsDTO convertOrderDataToDTO(final OrderData orderData) {
        return dataMapper.map(orderData, OrderWsDTO.class);
    }

    protected static void checkSuccessfulAuth(final AuthorizeResponseData authorizeResponseData) throws PaymentAuthorizationException {
        if (Boolean.FALSE.equals(authorizeResponseData.getIsSuccess())) {
            LOG.error("Error with the authorization process. Redirecting to payment method step.");
            throw new PaymentAuthorizationException();
        }
    }
}
