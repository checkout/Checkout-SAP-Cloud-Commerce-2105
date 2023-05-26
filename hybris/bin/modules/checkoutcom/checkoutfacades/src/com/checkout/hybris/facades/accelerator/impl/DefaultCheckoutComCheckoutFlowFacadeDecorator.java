package com.checkout.hybris.facades.accelerator.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.core.authorisation.AuthorizeResponse;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentService;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.AuthorizeResponseData;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import com.checkout.hybris.facades.constants.CheckoutFacadesConstants;
import com.checkout.GsonSerializer;
import com.checkout.payments.PaymentProcessed;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.PaymentResponse;
import com.checkout.payments.RequestSource;
import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of the {@link CheckoutComCheckoutFlowFacade}
 * Amends the ootb functionality of hasNoPaymentInfo and adds new methods for Checkout.com business logic.
 */
public class DefaultCheckoutComCheckoutFlowFacadeDecorator extends CheckoutComAbstractCheckoutFlowFacadeDecorator implements CheckoutComCheckoutFlowFacade {

    protected static final Logger LOG = LogManager.getLogger(DefaultCheckoutComCheckoutFlowFacadeDecorator.class);
    protected static final String CART_MODEL_NULL = "CartModel cannot be null";
    protected static final String APPROVED_RESPONSE_CODE = "10000";

    protected final CheckoutComAddressService addressService;
    protected final CheckoutComRequestFactory checkoutComRequestFactory;
    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;
    protected final CheckoutComPaymentInfoService paymentInfoService;
    protected final CheckoutComPaymentService paymentService;
    protected final Converter<AuthorizeResponse, AuthorizeResponseData> authorizeResponseConverter;


    public DefaultCheckoutComCheckoutFlowFacadeDecorator(final CheckoutFlowFacade checkoutFlowFacade,
                                                         final CheckoutComAddressService addressService,
                                                         final CheckoutComRequestFactory checkoutComRequestFactory,
                                                         final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService,
                                                         final CheckoutComPaymentInfoService paymentInfoService,
                                                         final CheckoutComPaymentService paymentService,
                                                         final Converter<AuthorizeResponse, AuthorizeResponseData> authorizeResponseConverter) {
        super(checkoutFlowFacade);
        this.addressService = addressService;
        this.checkoutComRequestFactory = checkoutComRequestFactory;
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
        this.paymentInfoService = paymentInfoService;
        this.paymentService = paymentService;
        this.authorizeResponseConverter = authorizeResponseConverter;
    }

    /**
     * Checks if there is no payment info. Changes all the ootb logic.
     *
     * @return true if there is not payment info in the checkout cart, false otherwise
     */
    @Override
    public boolean hasNoPaymentInfo() {
        final CartData cartData = getCheckoutCart();
        return cartData == null || (cartData.getPaymentInfo() == null && cartData.getCheckoutComPaymentInfo() == null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizeResponseData authorizePayment() {
        final AuthorizeResponseData authorizeResponseData = setUpAuthorizeResponseDefaultValues();

        final CartModel cart = getCart();
        validateParameterNotNull(cart, CART_MODEL_NULL);

        if (!callSuperCheckIfCurrentUserIsTheCartUser()) {
            LOG.error("The current user does not match the cart user");
            return authorizeResponseData;
        }

        if (!paymentInfoService.isValidPaymentInfo(cart)) {
            LOG.error("Payment info null or mandatory fields missing..");
            return authorizeResponseData;
        }

        updateResponseDataRequired(authorizeResponseData, cart);

        final PaymentResponse paymentResponse;
        try {
            final PaymentRequest<RequestSource> request = checkoutComRequestFactory.createPaymentRequest(cart);
            paymentResponse = checkoutComPaymentIntegrationService.authorizePayment(request);
            final GsonSerializer gsonSerializer = new GsonSerializer();
            final String requestJson = gsonSerializer.toJson(request);
            final String responseJson = gsonSerializer.toJson(paymentResponse);
            //Parse response and request
            paymentInfoService.saveRequestAndResponseInOrder(cart, requestJson, responseJson);
            paymentInfoService.logInfoOut(requestJson);
            paymentInfoService.logInfoOut(responseJson);
        } catch (final CheckoutComPaymentIntegrationException | IllegalArgumentException e) {
            LOG.error("Exception during authorization", e);
            authorizeResponseData.setIsSuccess(false);
            return authorizeResponseData;
        }
        final PaymentProcessed payment = paymentResponse.getPayment();

        if (isApprovedPayment(payment)) {
            return handleApprovedPaymentResponse(authorizeResponseData, cart, payment);
        } else if (isPendingPayment(paymentResponse)) {
            return authorizeResponseConverter.convert(paymentService.handlePendingPaymentResponse(paymentResponse.getPending(), cart.getPaymentInfo()));
        } else if (isFailedPayment(payment)) {
            handleFailedPaymentResponse(cart, payment);
        }

        LOG.error("Payment authorization response returned: approved [{}], pending [{}]", payment == null ? "null" : payment.isApproved(), paymentResponse.isPending());
        return authorizeResponseData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePaymentInfoFromSessionCart() {
        if (hasCheckoutCart()) {
            final CartModel sessionCart = getCart();
            if (sessionCart.getPaymentInfo() != null) {
                paymentInfoService.removePaymentInfo(sessionCart);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPaymentInfoBillingAddressOnSessionCart() {
        if (getCartService().hasSessionCart()) {
            final CartModel sessionCart = getCartService().getSessionCart();

            validateParameterNotNull(sessionCart.getPaymentInfo(), "PaymentInfo cannot be null once selected an existing payment method.");
            final AddressModel clonedAddressFromPaymentInfo = addressService.cloneAddress(sessionCart.getPaymentInfo().getBillingAddress());
            addressService.setCartPaymentAddress(sessionCart, clonedAddressFromPaymentInfo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserDataRequiredApmPaymentMethod() {
        return paymentInfoService.isUserDataRequiredApmPaymentMethod(getCart());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPaymentMethodType() {
        final CartData checkoutCart = getCheckoutCart();
        validateParameterNotNull(checkoutCart, "The current checkout cart cannot be null");

        final CheckoutComPaymentInfoData checkoutComPaymentInfoData = checkoutCart.getCheckoutComPaymentInfo();
        return checkoutComPaymentInfoData != null ? checkoutComPaymentInfoData.getType() : CheckoutFacadesConstants.CARD_PAYMENT_METHOD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpressCheckoutResult performExpressCheckout() {
        final ExpressCheckoutResult expressCheckoutResult = callSuperExpressCheckoutResult();
        setPaymentInfoBillingAddressOnSessionCart();
        return expressCheckoutResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressModel getDeliveryAddressModelForCode(final String addressId) {
        return super.getDeliveryAddressModelForCode(addressId);
    }

    protected AuthorizeResponseData setUpAuthorizeResponseDefaultValues() {
        final AuthorizeResponseData authorizeResponseData = new AuthorizeResponseData();
        authorizeResponseData.setIsSuccess(false);
        authorizeResponseData.setIsDataRequired(false);
        return authorizeResponseData;
    }

    protected void updateResponseDataRequired(final AuthorizeResponseData authorizeResponseData,
                                              final CartModel cartModel) {
        if (cartModel.getPaymentInfo() instanceof CheckoutComAPMPaymentInfoModel) {
            authorizeResponseData.setIsDataRequired(((CheckoutComAPMPaymentInfoModel) cartModel.getPaymentInfo()).getUserDataRequired());
        } else if (cartModel.getPaymentInfo() instanceof CheckoutComCreditCardPaymentInfoModel) {
            authorizeResponseData.setIsDataRequired(true);
        }
    }

    protected boolean isApprovedPayment(final PaymentProcessed payment) {
        return payment != null && payment.isApproved() && payment.getResponseCode().equalsIgnoreCase(APPROVED_RESPONSE_CODE);
    }

    protected AuthorizeResponseData handleApprovedPaymentResponse(
            final AuthorizeResponseData authorizeResponseData, final CartModel cartModel, final PaymentProcessed payment) {
        LOG.debug("Payment authorization success for cart with code [{}]. The redirect is not needed.", cartModel.getCode());
        if (cartModel.getPaymentInfo() instanceof CheckoutComCreditCardPaymentInfoModel) {
            paymentInfoService.addSubscriptionIdToUserPayment((CheckoutComCreditCardPaymentInfoModel) cartModel.getPaymentInfo(), payment.getSource());
        }
        paymentInfoService.addPaymentId(payment.getId(), cartModel.getPaymentInfo());
        authorizeResponseData.setIsSuccess(true);
        authorizeResponseData.setIsRedirect(false);
        return authorizeResponseData;
    }

    protected void handleFailedPaymentResponse(final CartModel cartModel, final PaymentProcessed payment) {
        LOG.debug("Storing failed payment ID in for cart with code [{}].", cartModel.getCode());
        paymentInfoService.addPaymentId(payment.getId(), cartModel.getPaymentInfo());
    }

    protected boolean isPendingPayment(final PaymentResponse paymentResponse) {
        return paymentResponse.isPending() && paymentResponse.getPending() != null;
    }

    protected boolean isFailedPayment(final PaymentProcessed payment) {
        return payment != null && !payment.isApproved();
    }

    protected ExpressCheckoutResult callSuperExpressCheckoutResult() {
        return super.performExpressCheckout();
    }

    protected boolean callSuperCheckIfCurrentUserIsTheCartUser() {
        return checkIfCurrentUserIsTheCartUser();
    }

}
