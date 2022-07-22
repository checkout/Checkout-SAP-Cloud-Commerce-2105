package com.checkout.hybris.facades.conversion.impl;

import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;
import com.checkout.hybris.facades.beans.GooglePaySelectionOption;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePayTransactionInfoData;
import com.checkout.hybris.facades.conversion.CheckoutComGooglePayConversionFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.List;

public class DefaultCheckoutComGooglePayConversionFacade  implements CheckoutComGooglePayConversionFacade {

    protected final Converter<GooglePaySettingsData, GooglePayMerchantConfigurationData> checkoutComGooglePayPaymentRequestConverter;
    protected final Converter<CartData, GooglePayTransactionInfoData> checkoutComGooglePayTransactionInfoConverter;
    protected final Converter<DeliveryModeData, GooglePaySelectionOption> checkoutComGooglePayDeliveryModeToSelectionOptionConverter;

    public DefaultCheckoutComGooglePayConversionFacade(final Converter<GooglePaySettingsData, GooglePayMerchantConfigurationData> checkoutComGooglePayPaymentRequestConverter,
                                                       final Converter<CartData, GooglePayTransactionInfoData> checkoutComGooglePayTransactionInfoConverter,
                                                       final Converter<DeliveryModeData, GooglePaySelectionOption> checkoutComGooglePayDeliveryModeToSelectionOptionConverter) {
        this.checkoutComGooglePayPaymentRequestConverter = checkoutComGooglePayPaymentRequestConverter;
        this.checkoutComGooglePayTransactionInfoConverter = checkoutComGooglePayTransactionInfoConverter;
        this.checkoutComGooglePayDeliveryModeToSelectionOptionConverter = checkoutComGooglePayDeliveryModeToSelectionOptionConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GooglePayMerchantConfigurationData getGooglePayMerchantConfiguration(final GooglePaySettingsData googlePaySettingsData) {
        return checkoutComGooglePayPaymentRequestConverter.convert(googlePaySettingsData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GooglePayTransactionInfoData getGooglePayTransactionInfo(final CartData cartData) {
        return checkoutComGooglePayTransactionInfoConverter.convert(cartData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GooglePaySelectionOption> getGooglePaySelectionOptions(final List<? extends DeliveryModeData> deliveryModeDataList) {
        return checkoutComGooglePayDeliveryModeToSelectionOptionConverter.convertAll(deliveryModeDataList);
    }
}
