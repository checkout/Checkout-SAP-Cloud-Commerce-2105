package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.POLI;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Poli apm payments
 */
public class CheckoutComPoliPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    public CheckoutComPoliPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
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
        return POLI;
    }
}
