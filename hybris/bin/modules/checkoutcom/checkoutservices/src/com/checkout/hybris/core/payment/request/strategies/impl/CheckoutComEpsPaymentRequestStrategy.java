package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import org.apache.commons.lang.StringUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.EPS;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Eps apm payments
 */
public class CheckoutComEpsPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String PURPOSE_KEY = "purpose";

    public CheckoutComEpsPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                final CheckoutComCurrencyService checkoutComCurrencyService,
                                                final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                final CMSSiteService cmsSiteService,
                                                final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService, checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return EPS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        validateParameterNotNull(cart, "Cart model cannot be null");

        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);
        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();

        validateParameterNotNull(cart, "Cart model cannot be null");
        checkArgument(StringUtils.isNotBlank(cart.getCheckoutComPaymentReference()), "Payment reference can not be blank or null");

        source.put(PURPOSE_KEY, cart.getCheckoutComPaymentReference());

        return paymentRequest;
    }
}
