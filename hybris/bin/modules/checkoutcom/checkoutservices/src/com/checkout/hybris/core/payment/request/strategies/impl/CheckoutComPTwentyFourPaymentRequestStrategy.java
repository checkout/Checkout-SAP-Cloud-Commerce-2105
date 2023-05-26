package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import org.apache.commons.lang.StringUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.P24;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for P24 apm payments
 */
@SuppressWarnings("java:S107")
public class CheckoutComPTwentyFourPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String PAYMENT_COUNTRY_KEY = "payment_country";
    protected static final String ACCOUNT_HOLDER_KEY = "account_holder_name";
    protected static final String ACCOUNT_HOLDER_EMAIL_KEY = "account_holder_email";

    protected final CheckoutComAddressService addressService;

    public CheckoutComPTwentyFourPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                        final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                        final CheckoutComCurrencyService checkoutComCurrencyService,
                                                        final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                        final CMSSiteService cmsSiteService,
                                                        final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                        final CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3Converter,
                                                        final CheckoutComAddressService addressService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService,
              checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService,
              checkoutComCartModelToPaymentL2AndL3Converter);
        this.addressService = addressService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return P24;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PaymentRequest<RequestSource> getRequestSourcePaymentRequest(final CartModel cart,
                                                                           final String currencyIsoCode, final Long amount) {
        final PaymentRequest<RequestSource> paymentRequest = super.getRequestSourcePaymentRequest(cart, currencyIsoCode, amount);
        final AlternativePaymentSource source = (AlternativePaymentSource) paymentRequest.getSource();

        final PaymentInfoModel paymentInfo = cart.getPaymentInfo();
        final AddressModel billingAddress = paymentInfo.getBillingAddress();

        validateParameterNotNull(billingAddress, "Billing address model cannot be null");
        validateParameterNotNull(billingAddress.getCountry(), "Billing address country cannot be null");

        final String countryIsoCode = billingAddress.getCountry().getIsocode();
        checkArgument(StringUtils.isNotEmpty(countryIsoCode), "Billing address country code cannot be null");
        final CustomerModel customer = (CustomerModel) paymentInfo.getUser();
        validateParameterNotNull(customer, "Customer model cannot be null");
        checkArgument(StringUtils.isNotEmpty(customer.getContactEmail()), "Customer email cannot be null");

        source.put(PAYMENT_COUNTRY_KEY, countryIsoCode);
        source.put(ACCOUNT_HOLDER_KEY, addressService.getCustomerFullNameFromAddress(billingAddress));
        source.put(ACCOUNT_HOLDER_EMAIL_KEY, customer.getContactEmail());

        return paymentRequest;
    }
}
