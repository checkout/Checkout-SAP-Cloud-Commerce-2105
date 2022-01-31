package com.checkout.hybris.facades.payment.info.populators.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Abstract implementation of {@link Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData>}
 * Populates type and billingAddress for APMs
 */
public class CheckoutComAbstractApmPaymentInfoPopulator implements Populator<CheckoutComAPMPaymentInfoModel, CheckoutComPaymentInfoData> {

    protected final Converter<AddressModel, AddressData> addressConverter;

    public CheckoutComAbstractApmPaymentInfoPopulator(final Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CheckoutComAPMPaymentInfoModel source, final CheckoutComPaymentInfoData target) throws ConversionException {
        validateParameterNotNull(source, "Parameter source cannot be null.");
        validateParameterNotNull(target, "Parameter target cannot be null.");

        target.setType(source.getType());
        target.setBillingAddress(addressConverter.convert(source.getBillingAddress()));
    }
}
