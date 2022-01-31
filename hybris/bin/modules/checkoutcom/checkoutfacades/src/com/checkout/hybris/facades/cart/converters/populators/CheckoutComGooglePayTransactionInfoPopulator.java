package com.checkout.hybris.facades.cart.converters.populators;

import com.checkout.hybris.facades.beans.GooglePayTransactionInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates attributes from {@link CartData}
 */
public class CheckoutComGooglePayTransactionInfoPopulator implements Populator<CartData, GooglePayTransactionInfoData> {

    protected static final String TOTAL_PRICE_STATUS = "FINAL";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CartData source, final GooglePayTransactionInfoData target) throws ConversionException {
        validateParameterNotNull(source, "CartData cannot be null.");
        validateParameterNotNull(target, "GooglePayTransactionInfo cannot be null.");

        target.setCurrencyCode(source.getTotalPrice().getCurrencyIso());
        target.setTotalPrice(source.getTotalPrice().getValue().toString());
        target.setTotalPriceStatus(TOTAL_PRICE_STATUS);
    }
}
