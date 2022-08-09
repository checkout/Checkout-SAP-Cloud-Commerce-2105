package com.checkout.hybris.facades.payment.google.impl;

import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.conversion.CheckoutComGooglePayConversionFacade;
import com.checkout.hybris.facades.merchant.CheckoutComMerchantConfigurationFacade;
import com.checkout.hybris.facades.payment.google.CheckoutComGooglePayFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.delivery.DeliveryService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.order.CartService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link CheckoutComGooglePayFacade}
 */
public class DefaultCheckoutComGooglePayFacade implements CheckoutComGooglePayFacade {

    protected static final String SHIPPING_OPTION_UNSELECTED = "shipping_option_unselected";

    protected final CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade;
    protected final CartService cartService;
    protected final CartFacade cartFacade;
    protected final CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade;
    protected final CheckoutComGooglePayConversionFacade checkoutComGooglePayConversionFacade;
    protected final DeliveryService deliveryService;
    protected final CheckoutComWalletAddressFacade checkoutComWalletAddressFacade;

    public DefaultCheckoutComGooglePayFacade(final CheckoutComMerchantConfigurationFacade checkoutComMerchantConfigurationFacade,
                                             final CartService cartService,
                                             final CartFacade cartFacade,
                                             final CheckoutComCheckoutFlowFacade checkoutComCheckoutFlowFacade,
                                             final CheckoutComGooglePayConversionFacade checkoutComGooglePayConversionFacade,
                                             final DeliveryService deliveryService,
                                             final CheckoutComWalletAddressFacade checkoutComWalletAddressFacade) {
        this.checkoutComMerchantConfigurationFacade = checkoutComMerchantConfigurationFacade;
        this.cartService = cartService;
        this.cartFacade = cartFacade;
        this.checkoutComCheckoutFlowFacade = checkoutComCheckoutFlowFacade;
        this.checkoutComGooglePayConversionFacade = checkoutComGooglePayConversionFacade;
        this.deliveryService = deliveryService;
        this.checkoutComWalletAddressFacade = checkoutComWalletAddressFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GooglePayMerchantConfigurationData getGooglePayMerchantConfiguration() {
        return checkoutComMerchantConfigurationFacade.getGooglePaySettings()
                .map(checkoutComGooglePayConversionFacade::getGooglePayMerchantConfiguration)
                .orElseThrow(() -> new IllegalArgumentException("Google Pay Configuration can not be null"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GooglePayPaymentDataRequestUpdate getGooglePayPaymentDataRequestUpdate(final GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) {
        final GooglePayPaymentDataRequestUpdate googlePayPaymentDataRequestUpdate = new GooglePayPaymentDataRequestUpdate();
        if (isCountrySupported(googlePayIntermediatePaymentData.getShippingAddress(), deliveryService.getDeliveryCountriesForOrder(cartService.getSessionCart()))) {
            googlePayPaymentDataRequestUpdate.setNewShippingOptionParameters(getNewShippingOptionParameters(googlePayIntermediatePaymentData));
            googlePayPaymentDataRequestUpdate.setNewTransactionInfo(checkoutComGooglePayConversionFacade.getGooglePayTransactionInfo(cartFacade.getSessionCart()));
        } else {
            googlePayPaymentDataRequestUpdate.setError(getCountryPaymentDataError());
        }

        return googlePayPaymentDataRequestUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GooglePayPaymentDataRequestUpdate getGooglePayDeliveryInfo(final GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) {
        checkoutComWalletAddressFacade.handleAndSaveShippingAddress(googlePayIntermediatePaymentData.getShippingAddress());

        if (hasDeliveryModeSelected(googlePayIntermediatePaymentData)) {
            checkoutComCheckoutFlowFacade.setDeliveryMode(googlePayIntermediatePaymentData.getShippingOptionData().getId());
        }
        return getGooglePayPaymentDataRequestUpdate(googlePayIntermediatePaymentData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GooglePayPaymentDataRequestUpdate getGooglePayPaymentDataRequestUpdate() {
        final GooglePayPaymentDataRequestUpdate googlePayPaymentDataRequestUpdate = new GooglePayPaymentDataRequestUpdate();
        googlePayPaymentDataRequestUpdate.setNewTransactionInfo(checkoutComGooglePayConversionFacade.getGooglePayTransactionInfo(cartFacade.getSessionCart()));

        return googlePayPaymentDataRequestUpdate;
    }

    private boolean isCountrySupported(final GooglePayIntermediateAddress shippingAddress, final Collection<CountryModel> deliveryCountries) {
        return deliveryCountries.stream()
                .map(CountryModel::getIsocode)
                .anyMatch(supportedIsocode -> shippingAddress.getCountryCode().equals(supportedIsocode));
    }

    protected GooglePayShippingOptionParameters getNewShippingOptionParameters(final GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) {
        final GooglePayShippingOptionParameters googlePayShippingOptionParameters = new GooglePayShippingOptionParameters();

        List<? extends DeliveryModeData> supportedDeliveryModes = checkoutComCheckoutFlowFacade.getSupportedDeliveryModes();
        googlePayShippingOptionParameters.setShippingOptions(checkoutComGooglePayConversionFacade.getGooglePaySelectionOptions(
                supportedDeliveryModes));

        if(hasDeliveryModeSelected(googlePayIntermediatePaymentData)){
            googlePayShippingOptionParameters.setDefaultSelectedOptionId(googlePayIntermediatePaymentData.getShippingOptionData().getId());
        }
        else{
            setDefaultShippingMode(googlePayShippingOptionParameters);
        }

        return googlePayShippingOptionParameters;
    }

    protected void setDefaultShippingMode(final GooglePayShippingOptionParameters googlePayShippingOptionParameters) {
        checkoutComCheckoutFlowFacade.getSupportedDeliveryModes().stream().findAny().ifPresent(deliveryModeData -> {
            checkoutComCheckoutFlowFacade.setDeliveryMode(deliveryModeData.getCode());
            googlePayShippingOptionParameters.setDefaultSelectedOptionId(deliveryModeData.getCode());
        });
    }

    protected GooglePayPaymentDataError getCountryPaymentDataError() {
        GooglePayPaymentDataError googlePayPaymentDataError = new GooglePayPaymentDataError();
        googlePayPaymentDataError.setIntent("SHIPPING_ADDRESS");
        googlePayPaymentDataError.setReason("SHIPPING_ADDRESS_UNSERVICEABLE");
        googlePayPaymentDataError.setMessage("Non-Deliverable country");

        return googlePayPaymentDataError;
    }


    /**
     * Checks if there is already a delivery mode selected
     *
     * @param googlePayIntermediatePaymentData the google payment date
     * @return true if there delivery mod eis selected, false otherwise
     */
    protected boolean hasDeliveryModeSelected(final GooglePayIntermediatePaymentData googlePayIntermediatePaymentData) {
        final GooglePaySelectionOptionData shippingOptionData = googlePayIntermediatePaymentData.getShippingOptionData();
        return shippingOptionData != null &&
                !SHIPPING_OPTION_UNSELECTED.equals(shippingOptionData.getId());
    }

}
