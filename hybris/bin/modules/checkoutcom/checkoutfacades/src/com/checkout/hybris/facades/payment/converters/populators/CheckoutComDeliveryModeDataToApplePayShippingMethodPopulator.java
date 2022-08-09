package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.facades.beans.ApplePayShippingMethod;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import java.util.Optional;

/**
 * Populated {@link ApplePayShippingMethod} from {@link DeliveryModeData}
 */
public class CheckoutComDeliveryModeDataToApplePayShippingMethodPopulator implements Populator<DeliveryModeData, ApplePayShippingMethod> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final DeliveryModeData deliveryModeData, final ApplePayShippingMethod applePayShippingMethod) throws ConversionException {
        applePayShippingMethod.setAmount(deliveryModeData.getDeliveryCost().getValue().toString());
        applePayShippingMethod.setLabel(Optional.ofNullable(deliveryModeData.getName()).orElseGet(deliveryModeData::getCode));
        applePayShippingMethod.setDetail(deliveryModeData.getDescription());
        applePayShippingMethod.setIdentifier(deliveryModeData.getCode());

    }
}
