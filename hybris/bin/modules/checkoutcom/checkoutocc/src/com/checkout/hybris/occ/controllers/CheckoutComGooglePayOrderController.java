package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.GooglePayMerchantConfigurationWsDTO;
import com.checkout.dto.order.PlaceWalletOrderWsDTO;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayAuthorisationRequest;
import com.checkout.hybris.facades.beans.GooglePayIntermediatePaymentData;
import com.checkout.hybris.facades.beans.GooglePayPaymentDataRequestUpdate;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import com.checkout.hybris.occ.exceptions.NoCheckoutCartException;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import java.util.Optional;

import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.FAILURE;
import static com.checkout.hybris.facades.enums.WalletPaymentType.GOOGLEPAY;
import static de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper.DEFAULT_LEVEL;

@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts/{cartId}/google")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
@Api(tags = "Orders")
public class CheckoutComGooglePayOrderController {

    @Resource(name = "userFacade")
    protected UserFacade userFacade;
    @Resource(name = "customerFacade")
    protected CheckoutComCustomerFacade defaultCheckoutComCustomerFacade;
    @Resource
    protected CheckoutComGooglePayFacade checkoutComGooglePayFacade;
    @Resource
    protected CheckoutComWalletOrderFacade checkoutComWalletOrderFacade;
    @Resource
    protected CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;
    @Resource
    protected DataMapper dataMapper;
    @Resource(name = "i18nService")
    protected I18NService i18nService;
    @Resource(name = "checkoutComCheckoutExpressPlaceOrderCartValidator")
    protected Validator checkoutComCheckoutExpressPlaceOrderCartValidator;
    @Resource(name = "messageSource")
    protected MessageSource messageSource;
    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;
    @Resource(name = "commerceWebServicesCartFacade2")
    private CartFacade cartFacade;

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

        if (defaultCheckoutComCustomerFacade.isGooglePayExpressGuestCustomer()) {
            defaultCheckoutComCustomerFacade.updateExpressCheckoutUserEmail(authorisationRequest.getEmail(), authorisationRequest.getShippingAddress().getName());
        }
        if (authorisationRequest.getBillingAddress() != null) {
            checkoutComWalletAddressFacade.handleAndSaveBillingAddress(authorisationRequest.getBillingAddress());
        }
        if (authorisationRequest.getShippingAddress() != null) {
            checkoutComWalletAddressFacade.handleAndSaveShippingAddress(authorisationRequest.getShippingAddress());
        }
        if(!cartFacade.getSessionCart().isCalculated()){
            checkoutFacade.prepareCartForCheckout();
        }
        try {
            validateGoogleCartForPlaceOrder();
            final PlaceWalletOrderDataResponse placeWalletOrderData = checkoutComWalletOrderFacade.placeWalletOrder(authorisationRequest.getToken(), GOOGLEPAY);
            Optional.ofNullable(placeWalletOrderData.getErrorMessage())
                    .map(errorMessage -> messageSource.getMessage(errorMessage, null, i18nService.getCurrentLocale()))
                    .ifPresent(placeWalletOrderData::setErrorMessage);

            return dataMapper.map(placeWalletOrderData, PlaceWalletOrderWsDTO.class, fields);
        } catch (WebserviceValidationException | NoCheckoutCartException e) {
            return dataMapper.map(createPlaceWalletOrderDateFailureResponse(), PlaceWalletOrderWsDTO.class, fields);
        }

    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_GUEST", "ROLE_TRUSTED_CLIENT", "ROLE_CLIENT"})
    @PostMapping(value = "/deliveryInfo", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @ApiOperation(nickname = "getGooglePayDeliveryInfo", value = "Creates a delivery address and delivery method for the cart.")
    public GooglePayPaymentDataRequestUpdate getGooglePayDeliveryInfo(@ApiParam(required = true) @RequestBody final GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) throws DuplicateUidException {
        if (userFacade.isAnonymousUser()) {
            defaultCheckoutComCustomerFacade.createGooglePayExpressCheckoutGuestUserForAnonymousCheckout();
        }
        return checkoutComGooglePayFacade.getGooglePayDeliveryInfo(googlePayIntermediatePaymentData);

    }

    /**
     * Verifies if the Google cart is valid for order place
     *
     * @throws NoCheckoutCartException When there is no checkout cart
     */
    protected void validateGoogleCartForPlaceOrder() throws NoCheckoutCartException {
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

    /**
     * Create a PlaceWalletOrderDataResponse with status FAILURE.
     *
     * @return the PlaceWalletOrderDataResponse with status FAILURE.
     */
    protected PlaceWalletOrderDataResponse createPlaceWalletOrderDateFailureResponse() {
        final PlaceWalletOrderDataResponse failurePlaceWalletOrderData = new PlaceWalletOrderDataResponse();
        failurePlaceWalletOrderData.setStatus(FAILURE);
        failurePlaceWalletOrderData.setErrorMessage(
                messageSource.getMessage("checkout.placeOrder.failed", null, i18nService.getCurrentLocale()));

        return failurePlaceWalletOrderData;
    }
}
