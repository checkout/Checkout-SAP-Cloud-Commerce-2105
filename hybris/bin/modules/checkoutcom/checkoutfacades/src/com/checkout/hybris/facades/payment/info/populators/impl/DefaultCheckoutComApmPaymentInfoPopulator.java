package com.checkout.hybris.facades.payment.info.populators.impl;

import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

/**
 * Populates the {@link CheckoutComPaymentInfoData} for all APMs
 * Extends {@link CheckoutComAbstractApmPaymentInfoPopulator}
 */
public class DefaultCheckoutComApmPaymentInfoPopulator extends CheckoutComAbstractApmPaymentInfoPopulator {

    public DefaultCheckoutComApmPaymentInfoPopulator(final Converter<AddressModel, AddressData> addressConverter) {
        super(addressConverter);
    }
}