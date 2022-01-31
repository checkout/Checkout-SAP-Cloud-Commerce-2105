package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.sources.SourceData;
import com.checkout.sources.SourceRequest;
import com.checkout.sources.SourceResponse;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for ACH apm payments
 */
public class CheckoutComAchPayPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String PAYMENT_SOURCE_ID_KEY = "id";
    protected static final String ACCOUNT_HOLDER_NAME_SOURCE_KEY = "account_holder_name";
    protected static final String ACCOUNT_TYPE_SOURCE_KEY = "account_type";
    protected static final String ACCOUNT_NUMBER_SOURCE_KEY = "account_number";
    protected static final String ROUTING_NUMBER_SOURCE_KEY = "routing_number";
    protected static final String BILLING_DESCRIPTOR_SOURCE_KEY = "billing_descriptor";
    protected static final String COMPANY_NAME_SOURCE_KEY = "company_name";

    protected CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;

    public CheckoutComAchPayPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                   final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                   final CheckoutComCurrencyService checkoutComCurrencyService,
                                                   final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                   final CMSSiteService cmsSiteService,
                                                   final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                   final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService, checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService);
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return ACH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest<RequestSource> createPaymentRequest(final CartModel cart) {
        validateParameterNotNull(cart, "Cart model cannot be null");

        final String currencyIsoCode = cart.getCurrency().getIsocode();
        final Long amount = checkoutComCurrencyService.convertAmountIntoPennies(currencyIsoCode, cart.getTotalPrice());

        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);

        final SourceResponse sourceResponse = getCheckoutComSourceResponse(cart);
        validateParameterNotNull(sourceResponse, "Checkout.com SourceResponse from set up payment source cannot be null");
        validateParameterNotNull(sourceResponse.getSource(), "Checkout.com SourceResponse Id from set up payment source response cannot be null");

        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();
        source.put(PAYMENT_SOURCE_ID_KEY, sourceResponse.getSource().getId());
        ((AlternativePaymentSource) paymentRequest.getSource()).setType(PAYMENT_SOURCE_ID_KEY);

        populatePaymentRequest(cart, paymentRequest);
        createCustomerRequestFromSource(sourceResponse.getSource()).ifPresent(paymentRequest::setCustomer);

        return paymentRequest;
    }

    /**
     * Gets the SourceResponse from checkout.com setup payment source integration call
     *
     * @param cart the cart model
     * @return the checkout.com SourceResponse
     */
    protected SourceResponse getCheckoutComSourceResponse(final CartModel cart) {
        SourceResponse sourceResponse = null;
        if (cart.getPaymentInfo() instanceof CheckoutComAchPaymentInfoModel) {
            final CheckoutComAchPaymentInfoModel achPaymentInfo = (CheckoutComAchPaymentInfoModel) cart.getPaymentInfo();
            final AddressModel billingAddress = achPaymentInfo.getBillingAddress();
            validateParameterNotNull(billingAddress, "Billing Address model cannot be null");
            try {
                sourceResponse = checkoutComPaymentIntegrationService.setUpPaymentSource(createSourceRequest(cart, achPaymentInfo, billingAddress));
            } catch (final CheckoutComPaymentIntegrationException e) {
                throw new IllegalArgumentException("Error setting the payment source with checkout.com endpoint for ach payment and cart " + cart.getCode());
            }
        } else {
            throw new IllegalArgumentException("The payment info doesn't match the CheckoutComAchPaymentInfoModel for cart number " + cart.getCode());
        }
        return sourceResponse;
    }

    /**
     * Creates the source request for the set up payment source request to checkout.com
     *
     * @param cart           the cart model
     * @param achPaymentInfo the payment info for ach
     * @param billingAddress the billing address
     * @return the populated SourceRequest
     */
    protected SourceRequest createSourceRequest(final CartModel cart, final CheckoutComAchPaymentInfoModel achPaymentInfo, final AddressModel billingAddress) {
        final SourceRequest sourceRequest = new SourceRequest();
        sourceRequest.setBillingAddress(createAddress(billingAddress));
        sourceRequest.setPhone(checkoutComPhoneNumberStrategy.createPhone(billingAddress).orElse(null));
        sourceRequest.setReference(cart.getCheckoutComPaymentReference());
        sourceRequest.setType(ACH.name());
        sourceRequest.setSourceData(createSourceData(achPaymentInfo));
        return sourceRequest;
    }

    /**
     * Creates the SourceData for the SourceRequest, with all mandatory fields
     *
     * @param achPaymentInfo the payment info for ach
     * @return the populate SourceData
     */
    protected SourceData createSourceData(final CheckoutComAchPaymentInfoModel achPaymentInfo) {
        final SourceData sourceData = new SourceData();
        sourceData.put(ACCOUNT_HOLDER_NAME_SOURCE_KEY, achPaymentInfo.getAccountHolderName());
        sourceData.put(ACCOUNT_TYPE_SOURCE_KEY, achPaymentInfo.getAccountType() != null ? achPaymentInfo.getAccountType().getCode() : null);
        sourceData.put(ACCOUNT_NUMBER_SOURCE_KEY, achPaymentInfo.getAccountNumber());
        sourceData.put(ROUTING_NUMBER_SOURCE_KEY, achPaymentInfo.getRoutingNumber());

        final BillingDescriptor billingDescriptor = checkoutComMerchantConfigurationService.getBillingDescriptor();
        validateParameterNotNull(billingDescriptor, "BillingDescriptor cannot be null");
        sourceData.put(BILLING_DESCRIPTOR_SOURCE_KEY, billingDescriptor.getBillingDescriptorName());

        if (StringUtils.isNotBlank(achPaymentInfo.getCompanyName())) {
            sourceData.put(COMPANY_NAME_SOURCE_KEY, achPaymentInfo.getCompanyName());
        }
        return sourceData;
    }
}
