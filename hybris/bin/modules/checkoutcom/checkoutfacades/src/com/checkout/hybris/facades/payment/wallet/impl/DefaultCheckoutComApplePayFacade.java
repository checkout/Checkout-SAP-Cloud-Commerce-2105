package com.checkout.hybris.facades.payment.wallet.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentFacade;
import com.checkout.hybris.facades.payment.wallet.CheckoutComApplePayFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link CheckoutComApplePayFacade}
 */
public class DefaultCheckoutComApplePayFacade implements CheckoutComApplePayFacade {

    protected static final String TOTAL_LINE_ITEM_TYPE = "final";
    protected static final String REQUIRED_POSTAL_ADDR = "postalAddress";
    public static final String APPLE_PAY_LINE_ITEM_TYPE_FINAL = "final";

    protected final CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade;
    protected final Converter<ApplePaySettingsData, ApplePayValidateMerchantData> checkoutComApplePayToValidateMerchantConverter;
    protected final Converter<DeliveryModeData, ApplePayShippingMethod> checkoutComDeliveryModeDataToApplePayShippingMethodConverter;
    protected final CheckoutComPaymentFacade checkoutComPaymentFacade;
    protected final CartFacade cartFacade;
    protected final CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade;

    public DefaultCheckoutComApplePayFacade(final CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade,
                                            final Converter<ApplePaySettingsData, ApplePayValidateMerchantData> checkoutComApplePayToValidateMerchantConverter,
                                            final Converter<DeliveryModeData, ApplePayShippingMethod> checkoutComDeliveryModeDataToApplePayShippingMethodConverter,
                                            final CheckoutComPaymentFacade checkoutComPaymentFacade,
                                            final CartFacade cartFacade,
                                            final CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade) {
        this.checkoutComMerchantConfigurationFacade = checkoutComMerchantConfigurationFacade;
        this.checkoutComApplePayToValidateMerchantConverter = checkoutComApplePayToValidateMerchantConverter;
        this.checkoutComDeliveryModeDataToApplePayShippingMethodConverter = checkoutComDeliveryModeDataToApplePayShippingMethodConverter;
        this.checkoutComPaymentFacade = checkoutComPaymentFacade;
        this.cartFacade = cartFacade;
        this.checkoutComCheckoutFlowFacade = checkoutComCheckoutFlowFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object requestApplePayPaymentSession(final ApplePayValidateMerchantRequestData validateMerchantRequestData) {
        final ApplePayValidateMerchantData validateMerchantData = getValidateMerchantData().orElse(null);

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(checkoutComPaymentFacade.createApplePayConnectionFactory())
                .build();

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory).postForObject(validateMerchantRequestData.getValidationURL(), validateMerchantData, Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplePayPaymentRequestData getApplePayPaymentRequest() {
        final ApplePayPaymentRequestData paymentRequest = new ApplePayPaymentRequestData();

        checkoutComMerchantConfigurationFacade.getApplePaySettings().ifPresentOrElse(applePaySettings -> {

            final CartData sessionCart = cartFacade.getSessionCart();
            final PriceData totalPrice = sessionCart.getTotalPrice();

            paymentRequest.setTotal(createApplePayTotalData(applePaySettings, totalPrice));
            paymentRequest.setMerchantCapabilities(applePaySettings.getMerchantCapabilities());
            paymentRequest.setSupportedNetworks(applePaySettings.getSupportedNetworks());
            paymentRequest.setCurrencyCode(totalPrice.getCurrencyIso());
            paymentRequest.setCountryCode(applePaySettings.getCountryCode());
            paymentRequest.setRequiredBillingContactFields(List.of(REQUIRED_POSTAL_ADDR));
        }, () -> {
            throw new IllegalArgumentException("ApplePay Configuration can not be null");
        });
        return paymentRequest;
    }

    @Override
    public ApplePayShippingContactUpdate getApplePayShippingContactUpdate() {
        final ApplePayShippingContactUpdate applePayShippingContactUpdate = new ApplePayShippingContactUpdate();

        final List<ApplePayShippingMethod> newShippingMethods = checkoutComDeliveryModeDataToApplePayShippingMethodConverter.convertAll(
                checkoutComCheckoutFlowFacade.getSupportedDeliveryModes());
        applePayShippingContactUpdate.setNewShippingMethods(newShippingMethods);

        Optional.ofNullable(newShippingMethods)
                .filter(CollectionUtils::isNotEmpty)
                .map(shippingMethods -> shippingMethods.iterator().next())
                .map(ApplePayShippingMethod::getIdentifier)
                .ifPresent(checkoutComCheckoutFlowFacade::setDeliveryMode);

        applePayShippingContactUpdate.setNewTotal(getApplePayLineTotalCartItem());
        return applePayShippingContactUpdate;
    }

    @Override
    public ApplePayShippingMethodUpdate getApplePayShippingMethodUpdate() {
        final ApplePayShippingMethodUpdate applePayShippingMethodUpdate = new ApplePayShippingMethodUpdate();
        applePayShippingMethodUpdate.setNewTotal(getApplePayLineTotalCartItem());

        return applePayShippingMethodUpdate;
    }

    protected ApplePayLineItem getApplePayLineTotalCartItem() {
        final ApplePayLineItem applePayLineItemCartTotal = new ApplePayLineItem();
        applePayLineItemCartTotal.setAmount(cartFacade.getSessionCart().getTotalPrice().getValue().toString());
        applePayLineItemCartTotal.setType(APPLE_PAY_LINE_ITEM_TYPE_FINAL);
        applePayLineItemCartTotal.setLabel("Total amount");
        return applePayLineItemCartTotal;
    }

    /**
     * Returns the apple pay validate merchant data
     */
    protected Optional<ApplePayValidateMerchantData> getValidateMerchantData() {
        return checkoutComMerchantConfigurationFacade.getApplePaySettings()
                .map(checkoutComApplePayToValidateMerchantConverter::convert);
    }

    /**
     * Creates the  applePay total item
     *
     * @param applePaySettings apple pay settings
     * @param totalPrice       cart total price
     */
    private ApplePayTotalData createApplePayTotalData(final ApplePaySettingsData applePaySettings, final PriceData totalPrice) {
        final ApplePayTotalData total = new ApplePayTotalData();
        total.setType(TOTAL_LINE_ITEM_TYPE);
        total.setLabel(applePaySettings.getMerchantName());
        total.setAmount(totalPrice.getValue().toString());
        return total;
    }
}
