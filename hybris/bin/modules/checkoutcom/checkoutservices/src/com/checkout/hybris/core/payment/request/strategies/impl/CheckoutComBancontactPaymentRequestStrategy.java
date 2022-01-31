package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
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
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.apache.commons.lang.StringUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.BANCONTACT;
import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * specific {@link CheckoutComPaymentRequestStrategy} implementation for Bancontact apm payments
 */
public class CheckoutComBancontactPaymentRequestStrategy extends CheckoutComAbstractApmPaymentRequestStrategy {

    protected static final String PAYMENT_COUNTRY_KEY = "payment_country";
    protected static final String ACCOUNT_HOLDER_KEY = "account_holder_name";

    protected final CheckoutComAddressService addressService;

    public CheckoutComBancontactPaymentRequestStrategy(final CheckoutComUrlService checkoutComUrlService,
                                                       final CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategy,
                                                       final CheckoutComCurrencyService checkoutComCurrencyService,
                                                       final CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapper,
                                                       final CMSSiteService cmsSiteService,
                                                       final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                       final CheckoutComAddressService addressService) {
        super(checkoutComUrlService, checkoutComPhoneNumberStrategy, checkoutComCurrencyService, checkoutComPaymentRequestStrategyMapper, cmsSiteService, checkoutComMerchantConfigurationService);
        this.addressService = addressService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CheckoutComPaymentType getStrategyKey() {
        return BANCONTACT;
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

        source.put(PAYMENT_COUNTRY_KEY, countryIsoCode);
        source.put(ACCOUNT_HOLDER_KEY, addressService.getCustomerFullNameFromAddress(billingAddress));

        return paymentRequest;
    }
}
