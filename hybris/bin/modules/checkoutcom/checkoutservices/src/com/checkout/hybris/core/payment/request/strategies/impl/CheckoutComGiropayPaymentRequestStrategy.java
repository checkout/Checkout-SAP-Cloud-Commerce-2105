package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.payments.ShippingDetails;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.GIROPAY;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Giropay apm payments
 */
public class CheckoutComGiropayPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    public CheckoutComGiropayPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                    final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                    final CheckoutComCurrencyService checkoutComCurrencyService,
                                                    final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                    final CMSSiteService cmsSiteService,
                                                    final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                    final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return GIROPAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);
        final ShippingDetails shippingDetails;
        final Address shippingAddress;

        validateParameterNotNull(cart, "Cart model cannot be null");
        checkArgument(StringUtils.isNotBlank(cart.getCheckoutComPaymentReference()), "Payment reference can not be blank or null");
        validateParameterNotNull(cart.getDeliveryAddress(), "Shipping address can not be null");
        final AddressModel deliveryAddress = cart.getDeliveryAddress();
        checkArgument(StringUtils.isNotBlank(deliveryAddress.getTown()), "City can not be blank or null");
        checkArgument(StringUtils.isNotBlank(deliveryAddress.getPostalcode()), "Postal code can not be blank or null");
        validateParameterNotNull(deliveryAddress.getCountry(), "Country can not be null");
        checkArgument(StringUtils.isNotBlank(deliveryAddress.getCountry().getIsocode()), "Country ISO code can not be blank or null");

        checkArgument(cart.getCheckoutComPaymentReference().length() <= 27, "Description of the product must have 27 characters");
        checkArgument(deliveryAddress.getTown().length() <= 50, "City can't have more than 50 characters");
        checkArgument(deliveryAddress.getPostalcode().length() <= 50, "Zip Code can't have more than 50 characters");
        checkArgument(deliveryAddress.getCountry().getIsocode().length() == 2, "Country ISO code must have 2 characters");

        paymentRequest.setDescription(cart.getCheckoutComPaymentReference());
        if(paymentRequest.getShipping() != null && paymentRequest.getShipping().getAddress() != null) {
            shippingAddress = paymentRequest.getShipping().getAddress();
        } else {
            shippingAddress = new Address();
            shippingDetails = new ShippingDetails(shippingAddress, null, null);
            paymentRequest.setShipping(shippingDetails);
        }
        shippingAddress.setCity(deliveryAddress.getTown());
        shippingAddress.setZip(deliveryAddress.getPostalcode());
        shippingAddress.setCountry(deliveryAddress.getCountry().getIsocode());

        return paymentRequest;
    }
}
