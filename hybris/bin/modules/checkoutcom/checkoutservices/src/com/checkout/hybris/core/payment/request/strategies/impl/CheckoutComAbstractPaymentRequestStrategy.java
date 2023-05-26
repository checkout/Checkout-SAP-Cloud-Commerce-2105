package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.enums.PaymentTypes;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;

import com.checkout.payments.*;
import com.checkout.sources.SourceProcessed;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Optional.empty;

/**
 * Abstract strategy to implement the common request population logic
 */
public abstract class CheckoutComAbstractPaymentRequestStrategy implements CheckoutComPaymentRequestStrategy {
    protected static final String SITE_ID_KEY = "site_id";
    protected static final String UDF1_KEY = "udf1";

    protected CMSSiteService cmsSiteService;
    protected CheckoutComUrlService checkoutComUrlService;
    protected CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy;
    protected CheckoutComCurrencyService checkoutComCurrencyService;
    protected CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper;
    protected CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter;

    protected CheckoutComAbstractPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                        final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                        final CheckoutComCurrencyService checkoutComCurrencyService,
                                                        final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                        final CMSSiteService cmsSiteService,
                                                        final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                        final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter) {
        this.checkoutComUrlService = checkoutComUrlService;
        this.checkoutComPhoneNumberStrategy = checkoutComPhoneNumberStrategy;
        this.checkoutComCurrencyService = checkoutComCurrencyService;
        this.checkoutComPaymentRequestStrategyMapper = checkoutComPaymentRequestStrategyMapper;
        this.cmsSiteService = cmsSiteService;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.checkoutComCartModelToPaymentL2AndL3Converter = checkoutComCartModelToPaymentL2AndL3Converter;
    }

    protected CheckoutComAbstractPaymentRequestStrategy() {
        // default empty constructor
    }

    /**
     * Add the strategy to the factory map of strategies
     */
    @PostConstruct
    protected void registerStrategy() {
        checkoutComPaymentRequestStrategyMapper.addStrategy(getStrategyKey(), this);
    }

    /**
     * Returns the key of the strategy used to register the strategy
     *
     * @return the key the strategy will be registered as
     */
    protected abstract CheckoutComPaymentType getStrategyKey();

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest<RequestSource> createPaymentRequest(final CartModel cart) {
        validateParameterNotNull(cart, "Cart model cannot be null");

        final String currencyIsoCode = cart.getCurrency().getIsocode();
        final Long amount = checkoutComCurrencyService.convertAmountIntoPennies(currencyIsoCode, cart.getTotalPrice());

        final PaymentRequest<RequestSource> request = getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);

        populatePaymentRequest(cart, request);

        return request;
    }

    /**
     * Populates the created request with all required information
     *
     * @param cart    the cart
     * @param request the request
     */
    protected void populatePaymentRequest(final CartModel cart, final PaymentRequest<RequestSource> request) {
        request.setReference(cart.getCheckoutComPaymentReference());
        request.setPaymentType(PaymentTypes.REGULAR.getCode());
        request.setCustomer(getCustomerRequest((CustomerModel) cart.getUser()));
        request.setRisk(new RiskRequest(true));
        request.setShipping(createShippingDetails(cart.getDeliveryAddress()));
        createThreeDSRequest().ifPresent(request::setThreeDS);
        isCapture().ifPresent(request::setCapture);

        populateRedirectUrls(request);
        populateRequestMetadata(request);
        populateDynamicBillingDescriptor(request);
        checkoutComCartModelToPaymentL2AndL3Converter.convert(cart, request);
    }


    /**
     * Populates the request's billing descriptor values based on the merchant configuration
     *
     * @param request the payment request
     */
    protected void populateDynamicBillingDescriptor(final PaymentRequest<RequestSource> request) {
        final BillingDescriptor billingDescriptorMerchantConfiguration =
                checkoutComMerchantConfigurationService.getBillingDescriptor();
        final Boolean includeBillingDescriptor = billingDescriptorMerchantConfiguration.getIncludeBillingDescriptor();
        if (Boolean.TRUE.equals(includeBillingDescriptor)) {
            final com.checkout.payments.BillingDescriptor billingDescriptor =
                    new com.checkout.payments.BillingDescriptor();
            billingDescriptor.setName(billingDescriptorMerchantConfiguration.getBillingDescriptorName());
            billingDescriptor.setCity(billingDescriptorMerchantConfiguration.getBillingDescriptorCity());
            request.setBillingDescriptor(billingDescriptor);
        }
    }

    /**
     * The optional empty value is by default, and checkout.com will take that as true.
     *
     * @return true if the payment is capture, false otherwise. By default optional empty.
     */
    protected Optional<Boolean> isCapture() {
        return empty();
    }

    /**
     * Populates failure and success urls
     *
     * @param request the request to populate
     */
    protected void populateRedirectUrls(final PaymentRequest<RequestSource> request) {
        request.setSuccessUrl(checkoutComUrlService.getFullUrl(cmsSiteService.getCurrentSite().getCheckoutComSuccessRedirectUrl(), true));
        request.setFailureUrl(checkoutComUrlService.getFullUrl(cmsSiteService.getCurrentSite().getCheckoutComFailureRedirectUrl(), true));
    }

    /**
     * Creates the PaymentRequest object based on token request or id request
     *
     * @param cart            the payment info
     * @param currencyIsoCode the currency code
     * @param amount          the order amount
     * @return the payment request object
     */
    protected abstract PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                                    final String currencyIsoCode,
                                                                                    final Long amount);

    /**
     * Creates a generic metadata for each request
     *
     * @return the metadata map
     */
    protected Map<String, Object> createGenericMetadata() {
        final Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put(SITE_ID_KEY, cmsSiteService.getCurrentSite().getUid());
        return metadataMap;
    }

    /**
     * Create the shipping details for the request
     *
     * @param deliveryAddress the address model
     * @return ShippingDetails the request object
     */
    protected ShippingDetails createShippingDetails(final AddressModel deliveryAddress) {
        final ShippingDetails shippingDetails = new ShippingDetails();
        if (deliveryAddress != null) {
            final Address address = createAddress(deliveryAddress);
            shippingDetails.setPhone(checkoutComPhoneNumberStrategy.createPhone(deliveryAddress).orElse(null));
            shippingDetails.setAddress(address);
        }
        return shippingDetails;
    }

    /**
     * Populates the address for the request
     *
     * @param addressModel the address source
     * @return the populated request address
     */
    protected Address createAddress(final AddressModel addressModel) {
        final Address address = new Address();
        address.setAddressLine1(addressModel.getLine1());
        address.setAddressLine2(addressModel.getLine2());
        address.setCity(addressModel.getTown());
        address.setCountry(addressModel.getCountry() != null ? addressModel.getCountry().getIsocode() : null);
        address.setState(addressModel.getRegion() != null ? addressModel.getRegion().getName() : null);
        address.setZip(addressModel.getPostalcode());
        return address;
    }

    /**
     * Creates the customer request
     *
     * @param customer session cart customer model
     * @return CustomerRequest the request object
     */
    protected CustomerRequest getCustomerRequest(final CustomerModel customer) {
        final CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setEmail(customer.getContactEmail());
        customerRequest.setName(customer.getDisplayName());
        return customerRequest;
    }

    /**
     * Creates the 3dsecure info object for the request. By default it's empty
     *
     * @return ThreeDSRequest the request object
     */
    protected Optional<ThreeDSRequest> createThreeDSRequest() {
        return empty();
    }

    /**
     * Populates the metadata in the request object.
     *
     * @param request the request payload
     */
    protected void populateRequestMetadata(final PaymentRequest<RequestSource> request) {
        request.setMetadata(createGenericMetadata());
    }

    /**
     * Creates the customer request using the source response id from checkout.com
     *
     * @param sourceProcessed the setup source response form checkout.com
     * @return CustomerRequest the request object
     */
    protected Optional<CustomerRequest> createCustomerRequestFromSource(final SourceProcessed sourceProcessed) {
        if (sourceProcessed.getCustomer() != null && StringUtils.isNotBlank(sourceProcessed.getCustomer().getId())) {
            final CustomerRequest customerRequest = new CustomerRequest();
            customerRequest.setId(sourceProcessed.getCustomer().getId());
            return Optional.of(customerRequest);
        }
        return Optional.empty();
    }
}
