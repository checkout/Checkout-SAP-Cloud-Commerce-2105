package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.facades.beans.GooglePaySelectionOption;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Optional;

/**
 * Populates googlepaySelectionOptions from the delivery mode data
 */
public class CheckoutComGooglePayDeliveryModeDataToSelectionOptionPopulator implements Populator<DeliveryModeData, GooglePaySelectionOption> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final DeliveryModeData deliveryModeData, final GooglePaySelectionOption googlePaySelectionOption) throws ConversionException {
        googlePaySelectionOption.setId(deliveryModeData.getCode());
        googlePaySelectionOption.setLabel(Optional.ofNullable(deliveryModeData.getName()).orElseGet(deliveryModeData::getCode));
        googlePaySelectionOption.setDescription(deliveryModeData.getDescription());
    }
}
