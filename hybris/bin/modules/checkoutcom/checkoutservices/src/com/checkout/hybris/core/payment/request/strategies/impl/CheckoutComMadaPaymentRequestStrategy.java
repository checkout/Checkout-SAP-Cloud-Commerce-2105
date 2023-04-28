package com.checkout.hybris.core.payment.request.strategies.impl;

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
import com.checkout.payments.ThreeDSRequest;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;

import java.util.Map;
import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MADA;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Mada card payments
 */
public class CheckoutComMadaPaymentRequestStrategy extends CheckoutComCardPaymentRequestStrategy {

    protected static final String MADA_VALUE = "mada";

    public CheckoutComMadaPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
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
        return MADA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Boolean> isCapture() {
        return empty();
    }

    /**
     * Populates the metadata in the request object. Includes a meta data key / value for "UDF1 = MADA"
     *
     * @param request the request payload
     */
    @Override
    protected void populateRequestMetadata(final PaymentRequest<RequestSource> request) {
        final Map<String, Object> metadataMap = createGenericMetadata();
        metadataMap.put(UDF1_KEY, MADA_VALUE);
        request.setMetadata(metadataMap);
    }

    /**
     * Create the 3dsecure info object for the request. For Mada payment it is always true
     *
     * @return ThreeDSRequest the request object
     */
    @Override
    protected Optional<ThreeDSRequest> createThreeDSRequest() {
        final ThreeDSRequest threeDSRequest = new ThreeDSRequest();
        threeDSRequest.setEnabled(true);
        return of(threeDSRequest);
    }
}
