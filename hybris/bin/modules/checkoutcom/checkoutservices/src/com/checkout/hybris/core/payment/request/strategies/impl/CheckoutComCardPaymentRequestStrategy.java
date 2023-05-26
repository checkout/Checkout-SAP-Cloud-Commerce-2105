package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;

import com.checkout.payments.*;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;

import java.util.Optional;

import static com.checkout.hybris.core.enums.PaymentActionType.AUTHORIZE_AND_CAPTURE;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.CARD;
import static java.lang.String.format;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for card payments
 */
public class CheckoutComCardPaymentRequestStrategy extends CheckoutComAbstractPaymentRequestStrategy implements CheckoutComPaymentRequestStrategy {

    public CheckoutComCardPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                 final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                 final CheckoutComCurrencyService checkoutComCurrencyService,
                                                 final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                 final CMSSiteService cmsSiteService,
                                                 final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                 final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return CARD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Boolean> isCapture() {
        return Optional.of(checkoutComMerchantConfigurationService.getPaymentAction().equals(AUTHORIZE_AND_CAPTURE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        final PaymentInfoModel paymentInfo = cart.getPaymentInfo();
        if (paymentInfo instanceof CheckoutComCreditCardPaymentInfoModel) {

            final CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfo = (CheckoutComCreditCardPaymentInfoModel) paymentInfo;
            if (paymentInfo.isSaved() && checkoutComCreditCardPaymentInfo.getSubscriptionId() != null) {
                return createIdSourcePaymentRequest(checkoutComCreditCardPaymentInfo, currencyIsoCode, amount);
            } else {
                return createTokenSourcePaymentRequest(checkoutComCreditCardPaymentInfo, currencyIsoCode, amount, cart.getPaymentAddress());
            }
        } else {
            throw new IllegalArgumentException(format("Strategy called with unsupported paymentInfo type : [%s] while trying to authorize cart: [%s]", paymentInfo.getClass().toString(), cart.getCode()));
        }
    }

    /**
     * Creates Payment request of type IdSource
     *
     * @param paymentInfo     from the cart
     * @param currencyIsoCode currency
     * @param amount          amount
     * @return paymentRequest to send to Checkout.com
     */
    protected PaymentRequest<RequestSource> createIdSourcePaymentRequest(final CheckoutComCreditCardPaymentInfoModel paymentInfo, final String currencyIsoCode, final Long amount) {
        return PaymentRequest.fromSource(new IdSource(paymentInfo.getSubscriptionId()), currencyIsoCode, amount);
    }

    /**
     * Creates Payment request of type Token source
     *
     * @param paymentInfo     from the cart
     * @param currencyIsoCode currency
     * @param amount          amount
     * @param billingAddress  to set in the request
     * @return paymentRequest to send to Checkout.com
     */
    protected PaymentRequest<RequestSource> createTokenSourcePaymentRequest(final CheckoutComCreditCardPaymentInfoModel paymentInfo, final String currencyIsoCode, final Long amount, final AddressModel billingAddress) {
        final PaymentRequest<RequestSource> paymentRequest = PaymentRequest.fromSource(new TokenSource(paymentInfo.getCardToken()), currencyIsoCode, amount);
        ((TokenSource) paymentRequest.getSource()).setBillingAddress(billingAddress != null ? createAddress(billingAddress) : null);
        return paymentRequest;
    }

    /**
     * Create the 3dsecure info object for the request.
     *
     * @return ThreeDSRequest the request object
     */
    @Override
    protected Optional<ThreeDSRequest> createThreeDSRequest() {
        final ThreeDSRequest threeDSRequest = new ThreeDSRequest();
        threeDSRequest.setEnabled(checkoutComMerchantConfigurationService.isThreeDSEnabled());
        threeDSRequest.setAttemptN3D(checkoutComMerchantConfigurationService.isAttemptNoThreeDSecure());
        return Optional.of(threeDSRequest);
    }
}
