package com.checkout.hybris.occ.controllers;

import com.checkout.dto.order.CheckoutPlaceOrderDto;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.occ.exceptions.NoCheckoutCartException;
import com.checkout.hybris.occ.exceptions.PlaceOrderException;
import com.checkout.hybris.occ.validators.impl.CheckoutComPlaceOrderCartValidator;
import com.checkout.payments.GetPaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commercewebservicescommons.annotation.SiteChannelRestriction;
import de.hybris.platform.commercewebservicescommons.dto.order.OrderWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.PaymentAuthorizationException;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdUserIdAndCartIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * Web Service Controller for the ORDERS resource. Methods require authentication
 * and are restricted to https channel.
 */
@RestController
@RequestMapping(value = "/{baseSiteId}/users/{userId}/carts/{cartId}")
@Api(tags = "Orders")
public class CheckoutComOrdersController {

    protected static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    protected static final String API_COMPATIBILITY_B2C_CHANNELS = "api.compatibility.b2c.channels";
    protected static final String CHECKOUTCOM_OCC_PLACE_ORDER_FAILED = "Failed to place the order";
    protected static final String PARAM_CKO_SESSION_ID = "cko-session-id";

    private static final Logger LOG = LogManager.getLogger(CheckoutComOrdersController.class);

    @Resource(name = "checkoutComPaymentFacade")
    protected CheckoutComPaymentFacade checkoutComPaymentFacade;
    @Resource(name = "checkoutComPaymentInfoFacade")
    protected CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;
    @Resource(name = "dataMapper")
    private DataMapper dataMapper;
    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;
    @Resource(name = "acceleratorCheckoutFacade")
    private AcceleratorCheckoutFacade acceleratorCheckoutFacadeCheckoutFacade;
    @Resource(name = "commerceWebServicesCartFacade2")
    private CartFacade cartFacade;
    @Resource(name = "checkoutFlowFacade")
    private CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    @Resource(name = "checkoutComPlaceOrderCartValidator")
    private CheckoutComPlaceOrderCartValidator checkoutComPlaceOrderCartValidator;

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST"})
    @PostMapping(value = "/direct-place-order")
    @ResponseStatus(HttpStatus.CREATED)
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2C_CHANNELS)
    @ApiOperation(nickname = "placeDirectOrder", value = "Place an order for direct APMs.", notes = "Authorizes the cart and places the order. The response contains the new order data.")
    @ApiBaseSiteIdUserIdAndCartIdParam
    public OrderWsDTO placeDirectOrder(
            @ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
            throws PaymentAuthorizationException, InvalidCartException, NoCheckoutCartException, PlaceOrderException {

        validateCartForPlaceOrder();
        if (!CheckoutComPaymentType.KLARNA.name().equalsIgnoreCase(checkoutFlowFacade.getCurrentPaymentMethodType())) {

            //authorize
            final AuthorizeResponseData authorizeResponseData = checkoutFlowFacade.authorizePayment();

            if (!authorizeResponseData.getIsSuccess() || Boolean.TRUE.equals(authorizeResponseData.getIsRedirect())) {
                if (Boolean.TRUE.equals(authorizeResponseData.getIsRedirect())) {
                    LOG.debug("Redirecting to checkout.com url [{}] for 3d secure.", authorizeResponseData.getRedirectUrl());
                    final OrderWsDTO orderWsDTO = new OrderWsDTO();
                    orderWsDTO.setRedirectUrl(authorizeResponseData.getRedirectUrl());
                    return orderWsDTO;
                } else {
                    LOG.error("Error with the authorization process. Redirecting to payment method step.");
                    throw new PaymentAuthorizationException();
                }
            }
        }

        final OrderData orderData;
        try {
            orderData = acceleratorCheckoutFacadeCheckoutFacade.placeOrder();
        } catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            checkoutFlowFacade.removePaymentInfoFromSessionCart();
            throw new PlaceOrderException();
        }

        return dataMapper.map(orderData, OrderWsDTO.class, fields);
    }

    @Secured({"ROLE_CUSTOMERGROUP", "ROLE_CLIENT", "ROLE_CUSTOMERMANAGERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_GUEST"})
    @PostMapping(value = "/redirect-place-order")
    @ResponseStatus(HttpStatus.CREATED)
    @SiteChannelRestriction(allowedSiteChannelsProperty = API_COMPATIBILITY_B2C_CHANNELS)
    @ApiOperation(nickname = "placeRedirectOrder", value = "Place an order for redirect APMs.", notes = "Place the order after APM success redirect. The response contains the new order data.")
    @ApiBaseSiteIdUserIdAndCartIdParam
    public OrderWsDTO placeRedirectOrder(
            @RequestBody final Object placeOrderDto,
            @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) throws PlaceOrderException {

        final CheckoutPlaceOrderDto checkoutPlaceOrderDto = convertObjectToPlaceOrderDto(placeOrderDto);
        if (StringUtils.isBlank(checkoutPlaceOrderDto.getCkoSessionId())) {
            LOG.error("Could not find valid cko-session-id");
            throw new PlaceOrderException(CHECKOUTCOM_OCC_PLACE_ORDER_FAILED);
        }

        boolean cartMatches = false;

        Optional<GetPaymentResponse> paymentDetails;
        try {
            paymentDetails = checkoutComPaymentFacade.getPaymentDetailsByCkoSessionId(checkoutPlaceOrderDto.getCkoSessionId());
            if (paymentDetails.isPresent()) {
                cartMatches = checkoutComPaymentFacade.doesSessionCartMatchAuthorizedCart(paymentDetails.get());
            }
        } catch (final CheckoutComPaymentIntegrationException e) {
            LOG.error("Exception when getting the payment from session id from checkout.com", e);
            // we need to void the authorization as well before the redirect
            checkoutFlowFacade.removePaymentInfoFromSessionCart();
            throw new PlaceOrderException(CHECKOUTCOM_OCC_PLACE_ORDER_FAILED);
        }

        if (!cartMatches) {
            LOG.error("Session cart matching not found.");
            throw new PlaceOrderException(CHECKOUTCOM_OCC_PLACE_ORDER_FAILED);
        }

        paymentDetails.ifPresent(responseDetails -> checkoutComPaymentInfoFacade.processPaymentDetails(responseDetails));

        try {
            return dataMapper.map(checkoutFacade.placeOrder(), OrderWsDTO.class, fields);
        } catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            checkoutFlowFacade.removePaymentInfoFromSessionCart();
            throw new PlaceOrderException(CHECKOUTCOM_OCC_PLACE_ORDER_FAILED);
        }
    }

    /**
     * Converts the input object to a {@link CheckoutPlaceOrderDto}
     *
     * @param object the object to convert
     * @return a {@link CheckoutPlaceOrderDto}
     * @throws PlaceOrderException if object format is invalid
     */
    protected CheckoutPlaceOrderDto convertObjectToPlaceOrderDto(final Object object) throws PlaceOrderException {
        try {
            return new ObjectMapper().convertValue(object, CheckoutPlaceOrderDto.class);
        } catch (final IllegalArgumentException e) {
            throw new PlaceOrderException(CHECKOUTCOM_OCC_PLACE_ORDER_FAILED);
        }
    }

    /**
     * Verifies if the cart is valid for order place
     *
     * @throws NoCheckoutCartException When there is no checkout cart
     * @throws InvalidCartException    When the checkout cart is invalid
     */
    protected void validateCartForPlaceOrder() throws NoCheckoutCartException, InvalidCartException {
        if (!checkoutFacade.hasCheckoutCart()) {
            throw new NoCheckoutCartException("Cannot place order. There was no checkout cart created yet!");
        }

        //Validate the cart
        List<CartModificationData> modifications;
        try {
            modifications = cartFacade.validateCartData();
        } catch (final CommerceCartModificationException e) {
            throw new InvalidCartException(e);
        }
        if (!modifications.isEmpty()) {
            throw new WebserviceValidationException(modifications);
        }

        final CartData cartData = cartFacade.getSessionCart();

        final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
        checkoutComPlaceOrderCartValidator.validate(cartData, errors);
        if (errors.hasErrors()) {
            throw new WebserviceValidationException(errors);
        }
    }
}
