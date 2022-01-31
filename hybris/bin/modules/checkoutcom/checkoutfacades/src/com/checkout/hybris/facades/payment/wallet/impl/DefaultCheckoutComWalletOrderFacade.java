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
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;

import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.FAILURE;
import static com.checkout.hybris.facades.enums.PlaceWalletOrderStatus.SUCCESS;

/**
 * Default implementation of {@link CheckoutComWalletOrderFacade}
 */
public class DefaultCheckoutComWalletOrderFacade implements CheckoutComWalletOrderFacade {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComWalletOrderFacade.class);

    protected final CheckoutComPaymentFacade checkoutComPaymentFacade;
    protected final MessageSource messageSource;
    protected final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade;
    protected final CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    protected final I18NService i18nService;

    public DefaultCheckoutComWalletOrderFacade(final CheckoutComPaymentFacade checkoutComPaymentFacade,
                                               final MessageSource messageSource,
                                               final CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacade,
                                               final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                               final I18NService i18nService) {
        this.checkoutComPaymentFacade = checkoutComPaymentFacade;
        this.messageSource = messageSource;
        this.checkoutComPaymentInfoFacade = checkoutComPaymentInfoFacade;
        this.checkoutFlowFacade = checkoutFlowFacade;
        this.i18nService = i18nService;
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
            return handleFailureProcess(response, messageSource.getMessage("checkout.error.authorization.failed", null, i18nService.getCurrentLocale()));
        }

        checkoutComPaymentInfoFacade.addPaymentInfoToCart(paymentInfoData);
        final AuthorizeResponseData authorizeResponseData = checkoutFlowFacade.authorizePayment();

        if (!authorizeResponseData.getIsSuccess()) {
            LOG.error("Error with the authorization process. Redirecting to payment method step.");
            return handleFailureProcess(response, messageSource.getMessage("checkout.error.authorization.failed", null, i18nService.getCurrentLocale()));
        }

        final OrderData orderData;
        try {
            orderData = checkoutFlowFacade.placeOrder();
        } catch (final InvalidCartException e) {
            LOG.error("Failed to place Order", e);
            return handleFailureProcess(response, messageSource.getMessage("checkout.placeOrder.failed", null, i18nService.getCurrentLocale()));
        }

        response.setStatus(SUCCESS);
        response.setOrderData(orderData);
        return response;
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
