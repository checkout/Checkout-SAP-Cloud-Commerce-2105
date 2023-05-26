package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.sources.SourceData;
import com.checkout.sources.SourceRequest;
import com.checkout.sources.SourceResponse;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SEPA;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Sepa apm payments
 */
public class CheckoutComSepaPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComSepaPaymentRequestStrategy.class);

    protected static final String ACCOUNT_IBAN_KEY = "account_iban";
    protected static final String MANDATE_TYPE_KEY = "mandate_type";
    protected static final String FIRST_NAME_KEY = "first_name";
    protected static final String LAST_NAME_KEY = "last_name";
    protected static final String BILLING_DESCRIPTOR_KEY = "billing_descriptor";
    protected static final String PAYMENT_SOURCE_ID_KEY = "id";

    protected final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService;

    public CheckoutComSepaPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                 final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                 final CheckoutComCurrencyService checkoutComCurrencyService,
                                                 final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                 final CMSSiteService cmsSiteService,
                                                 final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                 final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter,
                                                 final CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
        this.checkoutComPaymentIntegrationService = checkoutComPaymentIntegrationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return SEPA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest<RequestSource> createPaymentRequest(final CartModel cart) {
        validateParameterNotNull(cart, "Cart model cannot be null");

        final String currencyIsoCode = cart.getCurrency().getIsocode();
        final Long amount = checkoutComCurrencyService.convertAmountIntoPennies(currencyIsoCode, cart.getTotalPrice());

        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode,
                                                                                                  amount);

        final SourceResponse sourceResponse = getCheckoutComSourceResponse(cart);
        validateParameterNotNull(sourceResponse,
                                 "Checkout.com SourceResponse from set up payment source cannot be null");
        validateParameterNotNull(sourceResponse.getSource(),
                                 "Checkout.com SourceResponse Id from set up payment source response cannot be null");

        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();
        source.put(PAYMENT_SOURCE_ID_KEY, sourceResponse.getSource().getId());
        ((AlternativePaymentSource) paymentRequest.getSource()).setType(PAYMENT_SOURCE_ID_KEY);

        populatePaymentRequest(cart, paymentRequest);
        createCustomerRequestFromSource(sourceResponse.getSource()).ifPresent(paymentRequest::setCustomer);

        return paymentRequest;
    }


    protected SourceResponse getCheckoutComSourceResponse(final CartModel cart) {
        SourceResponse sourceResponse = null;
        if (cart.getPaymentInfo() instanceof CheckoutComSepaPaymentInfoModel) {
            final CheckoutComSepaPaymentInfoModel sepaPaymentInfo = (CheckoutComSepaPaymentInfoModel) cart.getPaymentInfo();
            try {
                sourceResponse = checkoutComPaymentIntegrationService.setUpPaymentSource(createSourceRequest(cart, sepaPaymentInfo));
            } catch (final CheckoutComPaymentIntegrationException e) {
                LOG.error("Error setting the payment source with checkout.com endpoint for sepa payment and cart [{}]", cart.getCode());
            }
        } else {
            throw new IllegalArgumentException("The payment info doesn't match the CheckoutComSepaPaymentInfoModel for cart number " + cart.getCode());
        }
        return sourceResponse;
    }

    /**
     * Creates the source request for the set up payment source request to checkout.com
     *
     * @param cart            the cart model
     * @param sepaPaymentInfo the payment info for SEPA
     * @return {@link SourceData} the populated SourceRequest
     */
    protected SourceRequest createSourceRequest(final CartModel cart, final CheckoutComSepaPaymentInfoModel sepaPaymentInfo) {
        final SourceRequest sourceRequest = new SourceRequest();
        sourceRequest.setBillingAddress(createAddress(sepaPaymentInfo));
        sourceRequest.setReference(cart.getCheckoutComPaymentReference());
        sourceRequest.setType(SEPA.name());
        sourceRequest.setSourceData(createSourceData(sepaPaymentInfo));
        return sourceRequest;
    }

    /**
     * Populates the SEPA address for the request
     *
     * @param sepaPaymentInfo the sepa payment info
     * @return the populated request address
     */
    protected Address createAddress(final CheckoutComSepaPaymentInfoModel sepaPaymentInfo) {
        final Address address = new Address();
        address.setAddressLine1(sepaPaymentInfo.getAddressLine1());
        address.setCity(sepaPaymentInfo.getCity());
        address.setCountry(sepaPaymentInfo.getCountry());
        address.setZip(sepaPaymentInfo.getPostalCode());
        return address;
    }

    /**
     * Creates the SourceData for the SourceRequest, with all mandatory fields
     *
     * @param sepaPaymentInfo the payment info for SEPA
     * @return the populate SourceData
     */
    protected SourceData createSourceData(final CheckoutComSepaPaymentInfoModel sepaPaymentInfo) {
        final SourceData sourceData = new SourceData();
        sourceData.put(FIRST_NAME_KEY, sepaPaymentInfo.getFirstName());
        sourceData.put(LAST_NAME_KEY, sepaPaymentInfo.getLastName());
        sourceData.put(ACCOUNT_IBAN_KEY, sepaPaymentInfo.getAccountIban());
        sourceData.put(MANDATE_TYPE_KEY, sepaPaymentInfo.getPaymentType().getCode().toLowerCase());

        final BillingDescriptor billingDescriptor = checkoutComMerchantConfigurationService.getBillingDescriptor();
        validateParameterNotNull(billingDescriptor, "BillingDescriptor cannot be null");
        sourceData.put(BILLING_DESCRIPTOR_KEY, billingDescriptor.getBillingDescriptorName());
        return sourceData;
    }
}
