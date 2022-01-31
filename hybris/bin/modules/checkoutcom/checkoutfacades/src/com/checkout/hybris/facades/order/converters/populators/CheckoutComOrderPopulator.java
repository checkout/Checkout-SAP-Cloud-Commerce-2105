package com.checkout.hybris.facades.order.converters.populators;

import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the billing address into the PaymentInfo for checkout.com guest checkout flow. The ccPaymentInfo is missing
 * for apms, so hybris cannot display the order confirmation for guest checkout due to null pointer exception in the
 * accelerator controller. This is the best way to fix the issue.
 */
public class CheckoutComOrderPopulator implements Populator<OrderModel, OrderData> {

    protected final Converter<AddressModel, AddressData> addressConverter;

    public CheckoutComOrderPopulator(final Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final OrderModel source, final OrderData target) throws ConversionException {
        validateParameterNotNull(source, "Parameter source cannot be null.");
        final PaymentInfoModel paymentInfo = source.getPaymentInfo();
        validateParameterNotNull(paymentInfo, "Payment info model source cannot be null.");
        validateParameterNotNull(target, "Parameter target cannot be null.");

        if (target.getPaymentInfo() == null) {
            final CCPaymentInfoData ccPaymentInfoData = new CCPaymentInfoData();
            ccPaymentInfoData.setBillingAddress(addressConverter.convert(paymentInfo.getBillingAddress()));
            target.setPaymentInfo(ccPaymentInfoData);
        }
    }
}
