package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.ApplePayPaymentRequestWsDTO;
import com.checkout.dto.order.ApplePayValidateMerchantRequestWsDTO;
import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import com.checkout.hybris.occ.exceptions.NoCheckoutCartException;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Optional;

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
    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;
    @Resource(name = "commerceWebServicesCartFacade2")
    private CartFacade cartFacade;
    @Resource(name = "checkoutComCheckoutExpressPlaceOrderCartValidator")
    protected Validator checkoutComCheckoutExpressPlaceOrderCartValidator;
    @Resource
    protected DataMapper dataMapper;
    @Resource(name = "userFacade")
    protected UserFacade userFacade;
    @Resource(name = "customerFacade")
    protected CheckoutComCustomerFacade defaultCheckoutComCustomerFacade;
    @Resource(name = "checkoutFlowFacade")
    private CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade;
    @Resource(name = "messageSource")
    protected MessageSource messageSource;
    @Resource(name = "i18nService")
    protected I18NService i18nService;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @GetMapping(value = "/paymentRequest", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(nickname = "getPaymentRequest", value = "Gets apple pay payment request")
    public ResponseEntity<ApplePayPaymentRequestWsDTO> getPaymentRequest(@RequestParam(required = false) final String productCode) throws CommerceCartModificationException {
        if(StringUtils.isNotEmpty(productCode)){
            cartFacade.addToCart(productCode,1L);
        }
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
                                            @RequestParam(defaultValue = DEFAULT_LEVEL) final String fields) throws NoCheckoutCartException {
        if (defaultCheckoutComCustomerFacade.isApplePayExpressGuestCustomer()) {
            defaultCheckoutComCustomerFacade.updateExpressCheckoutUserEmail(
                    authorisationRequest.getShippingContact().getEmailAddress(),
                    authorisationRequest.getShippingContact().getGivenName());
        }
        if (authorisationRequest.getBillingContact() != null) {
            checkoutComWalletAddressFacade.handleAndSaveBillingAddress(authorisationRequest.getBillingContact());
        }
        if (authorisationRequest.getShippingContact() != null) {
            checkoutComWalletAddressFacade.handleAndSaveShippingAddress(authorisationRequest.getShippingContact());
        }
        if(!cartFacade.getSessionCart().isCalculated()){
            checkoutFacade.prepareCartForCheckout();
        }
        validateCartForPlaceOrder();
        final PlaceWalletOrderDataResponse placeWalletOrderData = checkoutComWalletOrderFacade
                .placeWalletOrder(authorisationRequest.getToken().getPaymentData(), WalletPaymentType.APPLEPAY);

        Optional.ofNullable(placeWalletOrderData.getErrorMessage())
                .map(errorMessage -> messageSource.getMessage(errorMessage, null, i18nService.getCurrentLocale()))
                .ifPresent(placeWalletOrderData::setErrorMessage);

        return dataMapper.map(placeWalletOrderData, PlaceWalletOrderWsDTO.class, fields);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_GUEST", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/deliveryMethod")
    @ResponseBody
    @ApiOperation(nickname = "setDeliveryMethodOnCart", value = "Sets the deliveryMode for the cart.")
    public ApplePayShippingMethodUpdate setDeliveryMode(@ApiParam(required = true) @RequestBody final ApplePayShippingMethod applePayShippingMethod) {
        checkoutComCheckoutFlowFacade.setDeliveryMode(applePayShippingMethod.getIdentifier());
        return checkoutComApplePayFacade.getApplePayShippingMethodUpdate();
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/deliveryAddress", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @ApiOperation(nickname = "setDeliveryAddress", value = "Creates a delivery address for the cart.")
    public ApplePayShippingContactUpdate setDeliveryAddress(@ApiParam(required = true) @RequestBody final ApplePayPaymentContact applePayPaymentContact) throws DuplicateUidException {
        if (userFacade.isAnonymousUser()) {
            defaultCheckoutComCustomerFacade.createApplePayExpressCheckoutGuestUserForAnonymousCheckout();
        }
        checkoutComWalletAddressFacade.handleAndSaveShippingAddress(applePayPaymentContact);
        return checkoutComApplePayFacade.getApplePayShippingContactUpdate();
    }

    /**
     * Verifies if the  cart is valid for order place
     *
     * @throws NoCheckoutCartException When there is no checkout cart
     */
    protected void validateCartForPlaceOrder() throws NoCheckoutCartException {
        if (!checkoutFacade.hasCheckoutCart()) {
            throw new NoCheckoutCartException("Cannot place order. There was no checkout cart created yet!");
        }
        final CartData cartData = cartFacade.getSessionCart();
        final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
        checkoutComCheckoutExpressPlaceOrderCartValidator.validate(cartData, errors);
        if (errors.hasErrors()) {
            throw new WebserviceValidationException(errors);
        }
    }
}
