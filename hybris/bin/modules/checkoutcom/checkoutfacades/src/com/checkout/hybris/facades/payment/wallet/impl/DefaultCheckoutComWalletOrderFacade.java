package com.checkout.hybris.facades.payment.wallet.impl;

import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.beans.WalletPaymentInfoData;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComWalletOrderFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.FAILURE;
import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.SUCCESS;

/**
 * Default implementation of {@link CheckoutComWalletOrderFacade}
 */
public class DefaultCheckoutComWalletOrderFacade implements CheckoutComWalletOrderFacade {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComWalletOrderFacade.class);

    protected final CheckoutComPaymentFacade checkoutComPaymentFacade;
    protected final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;
    protected final CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    protected final CartFacade cartFacade;

    public DefaultCheckoutComWalletOrderFacade(final CheckoutComPaymentFacade checkoutComPaymentFacade,
                                               final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade,
                                               final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                               final CartFacade cartFacade) {
        this.checkoutComPaymentFacade = checkoutComPaymentFacade;
        this.checkoutComPaymentInfoFacade = checkoutComPaymentInfoFacade;
        this.checkoutFlowFacade = checkoutFlowFacade;
        this.cartFacade = cartFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaceWalletOrderDataResponse placeWalletOrder(final WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo,
                                                         final WalletPaymentType walletPaymentType) {
        final PlaceWalletOrderDataResponse response = new PlaceWalletOrderDataResponse();

        WalletPaymentInfoData paymentInfoData;
        try {
            paymentInfoData = checkoutComPaymentFacade.createCheckoutComWalletPaymentToken(walletPaymentAdditionalAuthInfo, walletPaymentType);
        } catch (final CheckoutComPaymentIntegrationException e) {
            LOG.error("Exception when trying to get the [{}] request token from checkout.com", walletPaymentType.name(), e);
            return handleFailureProcess(response, "checkout.error.authorization.failed");
        }

        checkoutComPaymentInfoFacade.addPaymentInfoToCart(paymentInfoData);
        final AuthorizeResponseData authorizeResponseData = checkoutFlowFacade.authorizePayment();

        if (Boolean.FALSE.equals(authorizeResponseData.getIsSuccess())) {
            LOG.error("Error with the authorization process. Redirecting to payment method step.");
            return handleFailureProcess(response, "checkout.error.authorization.failed");
        }
        if (authorizeResponseData.getIsRedirect()) {
           response.setRedirectUrl(authorizeResponseData.getRedirectUrl());
           return response;
        }

        final OrderData orderData;
        try {
            orderData = checkoutFlowFacade.placeOrder();
        } catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            return handleFailureProcess(response,"checkout.placeOrder.failed");
        }

        response.setStatus(SUCCESS);
        response.setOrderData(orderData);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateCartForPlaceOrder(final Validator checkoutComPlaceOrderCartValidator) throws InvalidCartException {
        final CartData cartData = cartFacade.getSessionCart();
        final Errors errors = new BeanPropertyBindingResult(cartData, "sessionCart");
        checkoutComPlaceOrderCartValidator.validate(cartData, errors);
        if (errors.hasErrors()) {
            throw new InvalidCartException(
                    errors.getAllErrors()
                            .stream()
                            .map(DefaultMessageSourceResolvable::getCode)
                            .filter(Objects::nonNull)
                            .reduce((errorCodes, errorCode) -> errorCodes.concat(",").concat(errorCode))
                            .orElse("Error during place Order on express checkout"));
        }
    }
    /**
     * Handles the wallet failure scenario
     *
     * @param response         the place order response
     * @param errorMessageCode the specific error message for the
     * @return the response populated for the given error
     */
    protected PlaceWalletOrderDataResponse handleFailureProcess(final PlaceWalletOrderDataResponse response, final String errorMessageCode) {
        checkoutFlowFacade.removePaymentInfoFromSessionCart();
        response.setStatus(FAILURE);
        response.setErrorMessage(errorMessageCode);
        return response;
    }

}
