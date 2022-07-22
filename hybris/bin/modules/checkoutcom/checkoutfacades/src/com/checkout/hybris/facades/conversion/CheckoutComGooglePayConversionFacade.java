package com.checkout.hybris.facades.conversion;

import com.checkout.hybris.facades.beans.GooglePayMerchantConfigurationData;
import com.checkout.hybris.facades.beans.GooglePaySelectionOption;
import com.checkout.hybris.facades.beans.GooglePaySettingsData;
import com.checkout.hybris.facades.beans.GooglePayTransactionInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;

import java.util.List;

public interface CheckoutComGooglePayConversionFacade {

    /**
     * Convert Google Pay settings to Google Pay Merchant configuration.
     *
     * @param googlePaySettingsData the Google Pay settings data.
     * @return                      the Google Pay merchant configuration.
     */
    GooglePayMerchantConfigurationData getGooglePayMerchantConfiguration(GooglePaySettingsData googlePaySettingsData);

    /**
     * Convert Cart data to Google Pay Transaction info.
     *
     * @param cartData the cart data.
     * @return         the Google Pay transaction info.
     */
    GooglePayTransactionInfoData getGooglePayTransactionInfo(CartData cartData);

    /**
     * Convert a list of Delivery modes to Google Pay selection options.
     *
     * @param deliveryModeDataList the list of delivery modes.
     * @return                     the list of Google Pay selection options.
     */
    List<GooglePaySelectionOption> getGooglePaySelectionOptions(List<? extends DeliveryModeData> deliveryModeDataList);
}
