package com.checkout.hybris.occ.strategies;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComAbstractPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;

/**
 * Abstract strategy that overrides redirect URLs for occ
 */
public abstract class CheckoutComOccAbstractPaymentRequestStrategy extends CheckoutComAbstractPaymentRequestStrategy {

    protected static final String CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_SUCCESS = "/order-confirmation?authorized" +
            "=true";
    protected static final String CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_FAILURE =
            "/order-confirmation?authorized=false";

    protected CheckoutComOccAbstractPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
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

    protected CheckoutComOccAbstractPaymentRequestStrategy() {
        // default empty constructor
    }

    /**
     * Populates failure and success urls for the occ
     *
     * @param request the request to populate
     */
    @Override
    protected void populateRedirectUrls(final PaymentRequest<RequestSource> request) {
        request.setSuccessUrl(checkoutComUrlService.getFullUrl(CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_SUCCESS, true));
        request.setFailureUrl(checkoutComUrlService.getFullUrl(CHECKOUT_COM_OCC_PAYMENT_REDIRECT_PAYMENT_FAILURE, true));
    }
}
