package com.checkout.hybris.core.payment.request.impl;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.CheckoutComRequestFactory;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;

import com.checkout.payments.*;
import com.google.common.collect.ImmutableMap;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.math.BigDecimal;
import java.util.Map;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of the {@link CheckoutComRequestFactory}
 */
public class DefaultCheckoutComRequestFactory implements CheckoutComRequestFactory {

    protected static final String INVALID_PAYMENT_REFERENCE = "Payment reference number cannot be null";
    protected static final String BUILD_VERSION_CONFIG_KEY = "build.version";
    protected static final String CHECKOUTSERVICES_CONNECTOR_VERSION_CONFIG_KEY = "checkoutservices.connector.version";
    protected static final String UDF5_KEY = "udf5";
    protected static final String DEFAULT_BUILD_VERSION = "develop";

    protected final CheckoutComCurrencyService checkoutComCurrencyService;
    protected final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper;
    protected final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    protected final ConfigurationService configurationService;

    public DefaultCheckoutComRequestFactory(final CheckoutComCurrencyService checkoutComCurrencyService,
                                            final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                            final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver,
                                            final ConfigurationService configurationService) {
        this.checkoutComCurrencyService = checkoutComCurrencyService;
        this.checkoutComPaymentRequestStrategyMapper = checkoutComPaymentRequestStrategyMapper;
        this.checkoutComPaymentTypeResolver = checkoutComPaymentTypeResolver;
        this.configurationService = configurationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest<RequestSource> createPaymentRequest(final CartModel cartModel) {
        validateParameterNotNull(cartModel, "Session cart model cannot be null");
        validateParameterNotNull(cartModel.getPaymentInfo(), "Payment info model cannot be null");

        final CheckoutComPaymentType paymentType = checkoutComPaymentTypeResolver.resolvePaymentType(cartModel.getPaymentInfo());

        final CheckoutComPaymentRequestStrategy paymentRequestStrategy = checkoutComPaymentRequestStrategyMapper.findStrategy(paymentType);
        final PaymentRequest<RequestSource> paymentRequest = paymentRequestStrategy.createPaymentRequest(cartModel);
        paymentRequest.getMetadata().putAll(createUdfFiveMetadata());
        return paymentRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CaptureRequest createCapturePaymentRequest(final BigDecimal amount, final String paymentReference, final String currencyCode) {
        validateParameterNotNull(amount, "Order amount cannot be null");
        validateParameterNotNull(paymentReference, INVALID_PAYMENT_REFERENCE);
        validateParameterNotNull(currencyCode, "Order currency code cannot be null");

        final CaptureRequest captureRequest = new CaptureRequest();
        captureRequest.setAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, amount.doubleValue()));
        captureRequest.setReference(paymentReference);
        captureRequest.setMetadata(createUdfFiveMetadata());
        return captureRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundRequest createRefundPaymentRequest(final BigDecimal amount, final String paymentReference, final String currencyCode) {
        validateParameterNotNull(amount, "Order amount cannot be null");
        validateParameterNotNull(paymentReference, INVALID_PAYMENT_REFERENCE);
        validateParameterNotNull(currencyCode, "Order currency code cannot be null");

        final RefundRequest refundRequest = new RefundRequest();
        refundRequest.setAmount(checkoutComCurrencyService.convertAmountIntoPennies(currencyCode, amount.doubleValue()));
        refundRequest.setReference(paymentReference);
        refundRequest.setMetadata(createUdfFiveMetadata());
        return refundRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VoidRequest createVoidPaymentRequest(final String paymentReference) {
        validateParameterNotNull(paymentReference, INVALID_PAYMENT_REFERENCE);

        final VoidRequest voidRequest = new VoidRequest();
        voidRequest.setReference(paymentReference);
        voidRequest.setMetadata(createUdfFiveMetadata());
        return voidRequest;
    }

    /**
     * Gets the generic metadata for every checkout.com request
     *
     * @return the generic metadata map
     */
    protected Map<String, Object> createUdfFiveMetadata() {
        final String buildVersion = configurationService.getConfiguration().getString(BUILD_VERSION_CONFIG_KEY);
        final String connectorVersion = configurationService.getConfiguration().getString(CHECKOUTSERVICES_CONNECTOR_VERSION_CONFIG_KEY, DEFAULT_BUILD_VERSION);
        final String udf5 = "hybris " + buildVersion + " extension " + connectorVersion;
        return ImmutableMap.of(UDF5_KEY, udf5);
    }
}
